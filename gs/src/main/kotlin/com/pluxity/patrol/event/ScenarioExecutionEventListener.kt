package com.pluxity.patrol.event

import com.pluxity.messaging.dto.ScenarioTriggerMessage
import io.github.springwolf.bindings.stomp.annotations.StompAsyncOperationBinding
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ScenarioExecutionEventListener(
    private val messagingTemplate: SimpMessagingTemplate,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val TOPIC_SCENARIO_EXECUTION = "/topic/scenario-execution"
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @AsyncPublisher(
        operation =
            AsyncOperation(
                channelName = TOPIC_SCENARIO_EXECUTION,
                description = "시나리오 트리거 발생 시 클라이언트에게 알림",
                payloadType = ScenarioTriggerMessage::class,
            ),
    )
    @StompAsyncOperationBinding
    fun handle(event: ScenarioExecutedEvent) {
        log.info("시나리오 실행 이벤트 처리: {}", event)
        messagingTemplate.convertAndSend(
            TOPIC_SCENARIO_EXECUTION,
            ScenarioTriggerMessage(event.scenarioExecutionId, event.scenarioId),
        )
    }
}
