package com.pluxity.patrol.dto

import com.pluxity.feature.entity.Spatial
import com.pluxity.patrol.constant.TriggerType
import com.pluxity.patrol.entity.Scenario
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "시나리오 응답")
data class ScenarioResponse(
    @field:Schema(description = "시나리오 ID", example = "1")
    val id: Long,
    @field:Schema(description = "시나리오 이름", example = "순찰 시나리오 1")
    val name: String,
    @field:Schema(description = "시나리오 설명", example = "1층 순찰 시나리오")
    val description: String?,
    @field:Schema(description = "활성화 여부", example = "true")
    val isActive: Boolean,
    @field:Schema(description = "시나리오에 포함된 씬 목록")
    val scenarioScenes: List<ScenarioSceneResponse>,
    @field:Schema(description = "시나리오 트리거 목록")
    val triggers: List<TriggerResponse>,
) {
    @Schema(description = "시나리오-씬 응답")
    data class ScenarioSceneResponse(
        @field:Schema(description = "시나리오-씬 ID", example = "1")
        val scenarioSceneId: Long,
        @field:Schema(description = "실행 순서 (1부터 시작)", example = "1")
        val order: Int,
        @field:Schema(description = "씬 기본 정보")
        val scene: SimpleSceneResponse,
        @field:Schema(description = "재생 시간 (초)", example = "10")
        val duration: Int?,
        @field:Schema(description = "전환 시간 (초)", example = "5")
        val transitionTime: Int,
    )

    @Schema(description = "씬 기본 정보")
    data class SimpleSceneResponse(
        @field:Schema(description = "씬 ID", example = "1")
        val sceneId: Long,
        @field:Schema(description = "씬 이름", example = "오전 순찰 모드")
        val sceneName: String,
        @field:Schema(description = "좌표 정보")
        val position: Spatial?,
        @field:Schema(description = "회전 정보")
        val rotation: Spatial?,
    )

    @Schema(description = "트리거 응답")
    data class TriggerResponse(
        @field:Schema(description = "트리거 ID", example = "1")
        val triggerId: Long,
        @field:Schema(description = "트리거 타입", example = "REPEAT")
        val triggerType: TriggerType,
        @field:Schema(description = "Cron 표현식 (UNIX 5자리 형식)", example = "30 9 * * 1,5")
        val cronExpression: String,
    )
}

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
