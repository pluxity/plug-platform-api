package com.pluxity.patrol.dto

import com.pluxity.feature.entity.Spatial
import com.pluxity.patrol.constant.DeviceAction
import com.pluxity.patrol.constant.DeviceType
import com.pluxity.patrol.entity.Scene
import com.pluxity.patrol.entity.SceneDeviceAction

data class SceneResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val duration: Int?,
    val position: Spatial?,
    val rotation: Spatial?,
    val facility: FacilitySummary,
    val sceneDeviceActions: List<SceneDeviceActionResponse>,
) {
    data class FacilitySummary(
        val id: Long,
        val name: String,
    )

    data class SceneDeviceActionResponse(
        val sceneDeviceActionId: Long,
        val deviceId: String,
        val deviceType: DeviceType,
        val deviceAction: DeviceAction,
        val actionParam: String?,
        val executionOrder: Int?,
    )
}

data class SceneListResponse(
    val id: Long,
    val name: String,
    val description: String?,
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
        facility = SceneResponse.FacilitySummary(facility.requiredId, facility.name),
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
