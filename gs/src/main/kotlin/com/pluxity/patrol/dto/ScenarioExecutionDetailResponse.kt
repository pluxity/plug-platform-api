package com.pluxity.patrol.dto

import com.pluxity.feature.entity.Spatial
import com.pluxity.patrol.entity.ScenarioExecution
import com.pluxity.patrol.entity.SceneExecution

data class ScenarioExecutionDetailResponse(
    val execution: ScenarioExecutionResponse,
    val sceneExecutions: List<SceneExecutionResponse>,
) {
    companion object {
        fun from(entity: ScenarioExecution) =
            ScenarioExecutionDetailResponse(
                execution = ScenarioExecutionResponse.from(entity),
                sceneExecutions = entity.sceneExecutions.map { SceneExecutionResponse.from(it) },
            )
    }
}

data class SceneExecutionResponse(
    val id: Long,
    val sceneName: String,
    val executionOrder: Int,
    val duration: Double,
    val position: Spatial?,
    val rotation: Spatial?,
) {
    companion object {
        fun from(entity: SceneExecution) =
            SceneExecutionResponse(
                id = entity.requiredId,
                sceneName = entity.sceneName,
                executionOrder = entity.executionOrder,
                duration = entity.duration,
                position = entity.position,
                rotation = entity.rotation,
            )
    }
}
