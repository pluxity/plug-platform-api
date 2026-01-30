package com.pluxity.patrol.dto

import com.pluxity.feature.entity.Spatial
import com.pluxity.patrol.constant.TriggerType
import com.pluxity.patrol.entity.Scenario

data class ScenarioResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val isActive: Boolean,
    val scenarioScenes: List<ScenarioSceneResponse>,
    val triggers: List<TriggerResponse>,
) {
    data class ScenarioSceneResponse(
        val scenarioSceneId: Long,
        val order: Int,
        val scene: SimpleSceneResponse,
        val duration: Int?,
        val transitionTime: Int,
    )

    data class SimpleSceneResponse(
        val sceneId: Long,
        val sceneName: String,
        val position: Spatial?,
        val rotation: Spatial?,
    )

    data class TriggerResponse(
        val triggerId: Long,
        val triggerType: TriggerType,
        val cronExpression: String,
    )
}

data class ScenarioListResponse(
    val id: Long,
    val name: String,
    val descriptor: String?,
    val isActive: Boolean,
)

fun Scenario.toResponse() =
    ScenarioResponse(
        id = requiredId,
        name = name,
        description = description,
        isActive = isActive,
        scenarioScenes =
            scenarioScenes.map { scenarioScene ->
                ScenarioResponse.ScenarioSceneResponse(
                    scenarioSceneId = scenarioScene.requiredId,
                    order = scenarioScene.executionOrder,
                    duration = scenarioScene.duration,
                    transitionTime = scenarioScene.transitionTime,
                    scene =
                        ScenarioResponse.SimpleSceneResponse(
                            sceneId = scenarioScene.scene.requiredId,
                            sceneName = scenarioScene.scene.name,
                            position = scenarioScene.scene.position,
                            rotation = scenarioScene.scene.rotation,
                        ),
                )
            },
        triggers =
            triggers.map { trigger ->
                ScenarioResponse.TriggerResponse(
                    triggerId = trigger.requiredId,
                    triggerType = trigger.triggerType,
                    cronExpression = trigger.cronExpression,
                )
            },
    )

fun Scenario.toListResponse() =
    ScenarioListResponse(
        id = requiredId,
        name = name,
        descriptor = description,
        isActive = isActive,
    )
