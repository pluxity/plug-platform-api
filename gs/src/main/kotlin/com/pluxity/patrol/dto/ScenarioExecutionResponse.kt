package com.pluxity.patrol.dto

import com.pluxity.patrol.constant.ScenarioExecutionStatus
import com.pluxity.patrol.constant.TriggerSource
import com.pluxity.patrol.entity.ScenarioExecution
import java.time.LocalDateTime

data class ScenarioExecutionResponse(
    val id: Long,
    val scenarioId: Long,
    val scenarioName: String,
    val triggerType: TriggerSource,
    val executionStatus: ScenarioExecutionStatus,
    val triggeredAt: LocalDateTime?,
    val startedAt: LocalDateTime?,
    val finishedAt: LocalDateTime?,
    val errorMessage: String?,
) {
    companion object {
        fun from(entity: ScenarioExecution) =
            ScenarioExecutionResponse(
                id = entity.requiredId,
                scenarioId = entity.scenario.requiredId,
                scenarioName = entity.scenario.name,
                triggerType = entity.triggerType,
                executionStatus = entity.executionStatus,
                triggeredAt = entity.triggeredAt,
                startedAt = entity.startedAt,
                finishedAt = entity.finishedAt,
                errorMessage = entity.errorMessage,
            )
    }
}
