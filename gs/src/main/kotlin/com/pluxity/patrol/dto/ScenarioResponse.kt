package com.pluxity.patrol.dto

import com.pluxity.feature.entity.Spatial
import com.pluxity.patrol.constant.TriggerType
import com.pluxity.patrol.entity.Scenario

data class ScenarioResponse(
    val id: Long,
    val facility: FacilitySummary,
    val name: String,
    val descriptor: String?,
    val isActive: Boolean,
    val scenarioScenes: List<ScenarioSceneResponse>,
    val triggers: List<TriggerResponse>,
) {
    data class FacilitySummary(
        val id: Long,
        val name: String,
    )

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

    companion object {
        fun from(scenario: Scenario): ScenarioResponse =
            ScenarioResponse(
                id = scenario.requiredId,
                facility =
                    FacilitySummary(
                        id = scenario.facility.requiredId,
                        name = scenario.facility.name,
                    ),
                name = scenario.name,
                descriptor = scenario.description,
                isActive = scenario.isActive,
                scenarioScenes =
                    scenario.scenarioScenes.map { scenarioScene ->
                        ScenarioSceneResponse(
                            scenarioSceneId = scenarioScene.requiredId,
                            order = scenarioScene.executionOrder,
                            duration = scenarioScene.duration,
                            transitionTime = scenarioScene.transitionTime,
                            scene =
                                SimpleSceneResponse(
                                    sceneId = scenarioScene.scene.id!!,
                                    sceneName = scenarioScene.scene.name,
                                    position = scenarioScene.scene.position,
                                    rotation = scenarioScene.scene.rotation,
                                ),
                        )
                    },
                triggers =
                    scenario.triggers.map { trigger ->
                        TriggerResponse(
                            triggerId = trigger.requiredId,
                            triggerType = trigger.triggerType,
                            cronExpression = trigger.cronExpression,
                        )
                    },
            )
    }
}

data class ScenarioListResponse(
    val id: Long,
    val name: String,
    val descriptor: String?,
    val isActive: Boolean,
) {
    companion object {
        fun from(scenario: Scenario): ScenarioListResponse =
            ScenarioListResponse(
                id = scenario.requiredId,
                name = scenario.name,
                descriptor = scenario.description,
                isActive = scenario.isActive,
            )
    }
}
