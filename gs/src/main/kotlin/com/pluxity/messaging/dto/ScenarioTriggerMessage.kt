package com.pluxity.messaging.dto

import com.pluxity.patrol.constant.TriggerTargetType

/** 배치 이벤트: 같은 시각에 발생한 트리거들을 묶어서 전달 */
data class ScenarioTriggerBatchEvent(
    val triggers: List<ScenarioTriggerInfo>,
)

data class ScenarioTriggerInfo(
    val scenarioExecutionId: Long,
    val scenarioId: Long,
    val targets: List<TriggerTargetInfo>,
)

/** 클라이언트 전송용 */
data class ScenarioTriggerMessage(
    val scenarioExecutionId: Long,
    val scenarioId: Long,
)

data class TriggerTargetInfo(
    val targetType: TriggerTargetType,
    val targetId: String,
)
