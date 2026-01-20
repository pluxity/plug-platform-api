package com.pluxity.messaging.dto

/** 서버 → 클라이언트: 트리거 발생 알림 */
data class ScenarioTriggerMessage(
    val scenarioExecutionId: Long,
    val scenarioId: Long,
)

/** 클라이언트 → 서버: 실행 시작 */
data class ScenarioExecutionStartMessage(
    val scenarioExecutionId: Long,
)

/** 클라이언트 → 서버: 실행 완료 */
data class ScenarioExecutionCompleteMessage(
    val scenarioExecutionId: Long,
    val success: Boolean,
    val errorMessage: String? = null,
)

/** 클라이언트 → 서버: 실행 취소 */
data class ScenarioExecutionCancelMessage(
    val scenarioExecutionId: Long,
)
