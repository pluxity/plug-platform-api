package com.pluxity.global.messaging.component

import org.springframework.stereotype.Component
import java.security.Principal
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.get

@Component
class SessionManager(
    private val userSessions: MutableMap<String, MutableList<Principal>> = ConcurrentHashMap(),
    private val userIndex: MutableMap<String, String> = ConcurrentHashMap(),
) {
    fun registerSession(
        userId: String,
        principal: Principal,
    ) {
        userSessions
            .computeIfAbsent(userId) { mutableListOf() }
            .add(principal)
        userIndex.computeIfAbsent(principal.name) { userId }
    }

    fun unregisterSession(principal: Principal) {
        val userId = userIndex[principal.name]
        userSessions[userId]?.remove(principal)
        userIndex.remove(principal.name)
        if (userSessions[userId]?.isEmpty() == true) {
            userSessions.remove(userId)
        }
    }

    fun findPrincipalByUserId(userId: String): List<Principal> = userSessions[userId] ?: emptyList()
}
