package com.pluxity.messaging

import com.pluxity.global.messaging.component.SessionManager
import com.pluxity.messaging.dto.ScenarioTriggerMessage
import com.pluxity.messaging.dto.TestMessage
import com.pluxity.messaging.dto.TriggerTargetInfo
import com.pluxity.patrol.constant.TriggerTargetType
import com.pluxity.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.springwolf.bindings.stomp.annotations.StompAsyncOperationBinding
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

private val log = KotlinLogging.logger {}

@Component
class StompMessageSender(
    private val messageTemplate: SimpMessagingTemplate,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
) {
    companion object {
        const val QUEUE_TEST_MESSAGE: String = "/queue/test-message"
        const val QUEUE_SCENARIO_EXECUTION = "/queue/scenario-execution"
    }

    @AsyncPublisher(
        operation =
            AsyncOperation(
                channelName = "/user${QUEUE_TEST_MESSAGE}",
                payloadType = TestMessage::class,
            ),
    )
    @StompAsyncOperationBinding
    fun sendTestMessage(
        payload: TestMessage,
        userIds: List<String>,
    ) {
        userIds.forEach { userId ->
            sessionManager.findPrincipalByUserId(userId).forEach { principal ->
                messageTemplate.convertAndSendToUser(principal.name, QUEUE_TEST_MESSAGE, payload)
            }
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @AsyncPublisher(
        operation =
            AsyncOperation(
                channelName = QUEUE_SCENARIO_EXECUTION,
                description = "시나리오 트리거 발생 시 클라이언트에게 알림",
                payloadType = ScenarioTriggerMessage::class,
            ),
    )
    @StompAsyncOperationBinding
    fun handle(event: ScenarioTriggerMessage) {
        val userIds = resolveUserIds(event.targets)

        userIds.forEach { userId ->
            sessionManager.findPrincipalByUserId(userId).forEach { principal ->
                log.info { "시나리오 실행 이벤트 처리: $userId : $event" }
                messageTemplate.convertAndSendToUser(principal.name, QUEUE_SCENARIO_EXECUTION, event)
            }
        }
    }

    private fun resolveUserIds(targets: List<TriggerTargetInfo>): Set<String> =
        targets
            .flatMap { target ->
                when (target.targetType) {
                    TriggerTargetType.USER -> listOf(target.targetId)
                    TriggerTargetType.ROLE -> userRepository.findUsernamesByRoleName(target.targetId)
                }
            }.toSet()
}
