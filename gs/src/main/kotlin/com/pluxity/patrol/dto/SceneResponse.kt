package com.pluxity.patrol.dto

import com.pluxity.feature.entity.Spatial
import com.pluxity.patrol.constant.DeviceAction
import com.pluxity.patrol.constant.DeviceType
import com.pluxity.patrol.entity.Scene

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

    companion object {
        fun from(scene: Scene): SceneResponse =
            SceneResponse(
                id = scene.requiredId,
                name = scene.name,
                description = scene.description,
                duration = scene.duration,
                position = scene.position,
                rotation = scene.rotation,
                facility =
                    FacilitySummary(
                        id = scene.facility.requiredId,
                        name = scene.facility.name,
                    ),
                sceneDeviceActions =
                    scene.sceneDeviceActions.map { action ->
                        SceneDeviceActionResponse(
                            sceneDeviceActionId = action.requiredId,
                            deviceId = action.deviceId,
                            deviceType = action.deviceType,
                            deviceAction = action.deviceAction,
                            actionParam = action.actionParam,
                            executionOrder = action.executionOrder,
                        )
                    },
            )
    }
}

data class SceneListResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val duration: Int?,
) {
    companion object {
        fun from(scene: Scene): SceneListResponse =
            SceneListResponse(
                id = scene.requiredId,
                name = scene.name,
                description = scene.description,
                duration = scene.duration,
            )
    }
}
