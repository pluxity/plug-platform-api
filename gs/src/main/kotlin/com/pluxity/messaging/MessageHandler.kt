package com.pluxity.messaging

import com.pluxity.messaging.dto.TestMessage
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.web.bind.annotation.RestController

@RestController
class MessageHandler(
    private val messageSender: StompMessageSender,
) {
    @MessageMapping("/test/message")
    fun testConnectionError(
        @Payload payload: TestMessage,
        headerAccessor: SimpMessageHeaderAccessor,
    ) {
        val username = headerAccessor.sessionAttributes?.get("username") as String
        messageSender.sendTestMessage(payload, listOf(username))
    }
}
