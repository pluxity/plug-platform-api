package com.pluxity.global.messaging.listeners

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.messaging.component.SessionManager
import org.springframework.context.event.EventListener
import org.springframework.messaging.Message
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class SocketApplicationEventListener(
    private val sessionManager: SessionManager,
) {
    @EventListener(SessionConnectedEvent::class)
    fun handleWebSocketConnectListener(event: SessionConnectedEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        val connectMessageHeader =
            headerAccessor
                .messageHeaders
                .get(SimpMessageHeaderAccessor.CONNECT_MESSAGE_HEADER, Message::class.java)
                ?: throw CustomException(ErrorCode.INVALID_FORMAT)
        val connectHeaderAccessor = StompHeaderAccessor.wrap(connectMessageHeader)
        val attributes = connectHeaderAccessor.sessionAttributes

        event.user?.let {
            sessionManager.registerSession(attributes?.get("username").toString(), it)
        }
    }

    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent) {
        event.user?.let {
            sessionManager.unregisterSession(it)
        }
    }
}
