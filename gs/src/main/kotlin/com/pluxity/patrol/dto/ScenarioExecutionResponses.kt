package com.pluxity.patrol.dto

import com.pluxity.feature.entity.Spatial
import com.pluxity.patrol.constant.DeviceAction
import com.pluxity.patrol.constant.DeviceType
import com.pluxity.patrol.constant.ScenarioExecutionStatus
import com.pluxity.patrol.constant.TriggerSource
import com.pluxity.patrol.entity.ScenarioExecution
import com.pluxity.patrol.entity.SceneDeviceActionExecution
import com.pluxity.patrol.entity.SceneExecution
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
)

data class ScenarioExecutionDetailResponse(
    val execution: ScenarioExecutionResponse,
    val sceneExecutions: List<SceneExecutionResponse>,
)

data class SceneExecutionResponse(
    val id: Long,
    val sceneName: String,
    val executionOrder: Int,
    val duration: Int,
    val position: Spatial?,
    val rotation: Spatial?,
    val sceneDeviceActionExecutionResponse: List<SceneDeviceActionExecutionResponse>,
)

data class SceneDeviceActionExecutionResponse(
    val id: Long,
    val deviceId: String,
    val deviceType: DeviceType,
    val deviceAction: DeviceAction,
    val actionParam: String?,
    val executionOrder: Int?,
)

data class ScenarioExecutionFailRequest(
    val errorMessage: String? = null,
)

fun ScenarioExecution.toResponse() =
    ScenarioExecutionResponse(
        id = requiredId,
        scenarioName = scenarioName,
        facilityName = facilityName,
        triggerType = triggerType,
        executionStatus = executionStatus,
        triggeredAt = triggeredAt,
        startedAt = startedAt,
        finishedAt = finishedAt,
        errorMessage = errorMessage,
        createBy = createdBy,
    )

fun ScenarioExecution.toDetailResponse() =
    ScenarioExecutionDetailResponse(
        execution = toResponse(),
        sceneExecutions = sceneExecutions.map { it.toResponse() },
    )

fun SceneExecution.toResponse() =
    SceneExecutionResponse(
        id = requiredId,
        sceneName = sceneName,
        executionOrder = executionOrder,
        duration = duration,
        position = position,
        rotation = rotation,
        sceneDeviceActionExecutionResponse = sceneDeviceActionExecutions.map { it.toResponse() },
    )

fun SceneDeviceActionExecution.toResponse() =
    SceneDeviceActionExecutionResponse(
        id = requiredId,
        deviceId = deviceId,
        deviceType = deviceType,
        deviceAction = deviceAction,
        actionParam = actionParam,
        executionOrder = executionOrder,
    )
