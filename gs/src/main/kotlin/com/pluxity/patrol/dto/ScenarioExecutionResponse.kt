package com.pluxity.patrol.dto

import com.pluxity.patrol.constant.ScenarioExecutionStatus
import com.pluxity.patrol.constant.TriggerSource
import com.pluxity.patrol.entity.ScenarioExecution
import java.time.LocalDateTime

data class ScenarioExecutionResponse(
    val id: Long,
    val scenarioName: String,
    val facilityName: String,
    val triggerType: TriggerSource,
    val executionStatus: ScenarioExecutionStatus,
    val triggeredAt: LocalDateTime?,
    val startedAt: LocalDateTime?,
    val finishedAt: LocalDateTime?,
    val errorMessage: String?,
    val createBy: String?,
) {
    companion object {
        fun from(entity: ScenarioExecution) =
            ScenarioExecutionResponse(
                id = entity.requiredId,
                scenarioName = entity.scenarioName,
                facilityName = entity.facilityName,
                triggerType = entity.triggerType,
                executionStatus = entity.executionStatus,
                triggeredAt = entity.triggeredAt,
                startedAt = entity.startedAt,
                finishedAt = entity.finishedAt,
                errorMessage = entity.errorMessage,
                createBy = entity.createdBy,
            )
    }
}
