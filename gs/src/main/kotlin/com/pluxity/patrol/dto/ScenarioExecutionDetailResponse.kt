package com.pluxity.patrol.dto

import com.pluxity.patrol.constant.ScenarioExecutionStatus
import com.pluxity.patrol.constant.SceneExecutionStatus
import com.pluxity.patrol.constant.TriggerSource
import com.pluxity.patrol.entity.ScenarioExecution
import com.pluxity.patrol.entity.SceneExecution
import java.time.LocalDateTime

data class ScenarioExecutionDetailResponse(
    val id: Long,
    val scenarioId: Long,
    val scenarioName: String,
    val triggerType: TriggerSource,
    val executionStatus: ScenarioExecutionStatus,
    val triggeredAt: LocalDateTime?,
    val startedAt: LocalDateTime?,
    val finishedAt: LocalDateTime?,
    val errorMessage: String?,
    val sceneExecutions: List<SceneExecutionResponse>,
) {
    companion object {
        fun from(entity: ScenarioExecution) =
            ScenarioExecutionDetailResponse(
                id = entity.requiredId,
                scenarioId = entity.scenario.requiredId,
                scenarioName = entity.scenario.name,
                triggerType = entity.triggerType,
                executionStatus = entity.executionStatus,
                triggeredAt = entity.triggeredAt,
                startedAt = entity.startedAt,
                finishedAt = entity.finishedAt,
                errorMessage = entity.errorMessage,
                sceneExecutions = entity.sceneExecutions.map { SceneExecutionResponse.from(it) },
            )
    }
}

data class SceneExecutionResponse(
    val id: Long,
    val sceneId: Long,
    val sceneName: String,
    val status: SceneExecutionStatus,
    val startedAt: LocalDateTime?,
    val finishedAt: LocalDateTime?,
    val errorMessage: String?,
) {
    companion object {
        fun from(entity: SceneExecution) =
            SceneExecutionResponse(
                id = entity.requiredId,
                sceneId = entity.scene.requiredId,
                sceneName = entity.scene.name,
                status = entity.status,
                startedAt = entity.startedAt,
                finishedAt = entity.finishedAt,
                errorMessage = entity.errorMessage,
            )
    }
}
