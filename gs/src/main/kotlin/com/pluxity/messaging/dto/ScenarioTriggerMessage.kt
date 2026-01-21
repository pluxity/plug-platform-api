package com.pluxity.messaging.dto

/** 서버 → 클라이언트: 트리거 발생 알림 */
data class ScenarioTriggerMessage(
    val scenarioExecutionId: Long,
    val scenarioId: Long,
)
