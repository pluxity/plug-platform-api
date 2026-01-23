package com.pluxity.messaging.dto

import com.pluxity.patrol.constant.TriggerTargetType

/** 서버 내부 이벤트 전달용 */
data class ScenarioTriggerEvent(
    val scenarioExecutionId: Long,
    val scenarioId: Long,
    val targets: List<TriggerTargetInfo>,
) {
    fun toMessage() =
        ScenarioTriggerMessage(
            scenarioExecutionId = scenarioExecutionId,
            scenarioId = scenarioId,
        )
}

/** 클라이언트 전송용 */
data class ScenarioTriggerMessage(
    val scenarioExecutionId: Long,
    val scenarioId: Long,
)

data class TriggerTargetInfo(
    val targetType: TriggerTargetType,
    val targetId: String,
)
