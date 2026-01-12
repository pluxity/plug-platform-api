package com.pluxity.messaging

import com.pluxity.global.messaging.component.SessionManager
import com.pluxity.messaging.dto.TestMessage
import io.github.springwolf.bindings.stomp.annotations.StompAsyncOperationBinding
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class StompMessageSender(
    private val messageTemplate: SimpMessagingTemplate,
    private val sessionManager: SessionManager,
) {
    companion object {
        const val QUEUE_TEST_MESSAGE: String = "/queue/test-message"
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
}
