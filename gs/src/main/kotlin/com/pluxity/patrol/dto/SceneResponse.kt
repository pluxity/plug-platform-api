package com.pluxity.patrol.dto

import com.pluxity.feature.entity.Spatial
import com.pluxity.patrol.constant.DeviceAction
import com.pluxity.patrol.constant.DeviceType
import com.pluxity.patrol.entity.Scene
import com.pluxity.patrol.entity.SceneDeviceAction
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "씬 응답")
data class SceneResponse(
    @field:Schema(description = "씬 ID", example = "1")
    val id: Long,
    @field:Schema(description = "씬 이름", example = "오전 순찰 모드")
    val name: String,
    @field:Schema(description = "씬 설명", example = "오전 9시 자동으로 실행되는 순찰 씬")
    val description: String?,
    @field:Schema(description = "유지 시간 (초)", example = "10")
    val duration: Int?,
    @field:Schema(description = "좌표 정보")
    val position: Spatial?,
    @field:Schema(description = "회전 정보")
    val rotation: Spatial?,
    @field:Schema(description = "씬에 포함된 기기 액션 목록")
    val sceneDeviceActions: List<SceneDeviceActionResponse>,
) {
    @Schema(description = "씬의 기기 액션 응답")
    data class SceneDeviceActionResponse(
        @field:Schema(description = "기기 액션 ID", example = "1")
        val sceneDeviceActionId: Long,
        @field:Schema(description = "기기 고유 식별자", example = "DEV-001")
        val deviceId: String,
        @field:Schema(description = "기기 타입", example = "CAMERA")
        val deviceType: DeviceType,
        @field:Schema(description = "실행 액션", example = "START")
        val deviceAction: DeviceAction,
        @field:Schema(description = "액션 파라미터", example = "pan_left")
        val actionParam: String?,
        @field:Schema(description = "실행 순서", example = "1")
        val executionOrder: Int?,
    )
}

@Schema(description = "씬 목록 응답")
data class SceneListResponse(
    @field:Schema(description = "씬 ID", example = "1")
    val id: Long,
    @field:Schema(description = "씬 이름", example = "오전 순찰 모드")
    val name: String,
    @field:Schema(description = "씬 설명", example = "오전 9시 자동으로 실행되는 순찰 씬")
    val description: String?,
    @field:Schema(description = "유지 시간 (초)", example = "10")
    val duration: Int?,
)

fun Scene.toResponse() =
    SceneResponse(
        id = requiredId,
        name = name,
        description = description,
        duration = duration,
        position = position,
        rotation = rotation,
        sceneDeviceActions = sceneDeviceActions.map { it.toResponse() },
    )

fun SceneDeviceAction.toResponse() =
    SceneResponse.SceneDeviceActionResponse(
        sceneDeviceActionId = requiredId,
        deviceId = deviceId,
        deviceType = deviceType,
        deviceAction = deviceAction,
        actionParam = actionParam,
        executionOrder = executionOrder,
    )

fun Scene.toListResponse() =
    SceneListResponse(
        id = requiredId,
        name = name,
        description = description,
        duration = duration,
    )
