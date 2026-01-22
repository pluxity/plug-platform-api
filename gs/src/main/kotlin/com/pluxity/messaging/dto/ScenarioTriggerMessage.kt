package com.pluxity.messaging.dto

import com.pluxity.patrol.constant.TriggerTargetType

/** 서버 → 클라이언트: 트리거 발생 알림 */
data class ScenarioTriggerMessage(
    val scenarioExecutionId: Long,
    val scenarioId: Long,
    val targets: List<TriggerTargetInfo>,
)

data class TriggerTargetInfo(
    val targetType: TriggerTargetType,
    val targetId: String,
)
