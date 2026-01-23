package com.pluxity.patrol.dto

import com.pluxity.feature.entity.Spatial
import com.pluxity.patrol.constant.DeviceAction
import com.pluxity.patrol.constant.DeviceType
import com.pluxity.patrol.entity.ScenarioExecution
import com.pluxity.patrol.entity.SceneDeviceActionExecution
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
    val sceneDeviceActionExecutionResponse: List<SceneDeviceActionExecutionResponse>,
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
                sceneDeviceActionExecutionResponse =
                    entity.sceneDeviceActionExecutions.map {
                        SceneDeviceActionExecutionResponse.from(it)
                    },
            )
    }
}

data class SceneDeviceActionExecutionResponse(
    val id: Long,
    val deviceId: String,
    val deviceType: DeviceType,
    val deviceAction: DeviceAction,
    val actionParam: String?,
    val executionOrder: Int?,
) {
    companion object {
        fun from(entity: SceneDeviceActionExecution) =
            SceneDeviceActionExecutionResponse(
                id = entity.requiredId,
                deviceId = entity.deviceId,
                deviceType = entity.deviceType,
                deviceAction = entity.deviceAction,
                actionParam = entity.actionParam,
                executionOrder = entity.executionOrder,
            )
    }
}
