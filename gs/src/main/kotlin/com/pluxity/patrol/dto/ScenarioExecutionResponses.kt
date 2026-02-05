package com.pluxity.patrol.dto

import com.pluxity.feature.entity.Spatial
import com.pluxity.patrol.constant.DeviceAction
import com.pluxity.patrol.constant.DeviceType
import com.pluxity.patrol.constant.ScenarioExecutionStatus
import com.pluxity.patrol.constant.TriggerSource
import com.pluxity.patrol.entity.ScenarioExecution
import com.pluxity.patrol.entity.SceneDeviceActionExecution
import com.pluxity.patrol.entity.SceneExecution
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "시나리오 실행 응답")
data class ScenarioExecutionResponse(
    @field:Schema(description = "시나리오 실행 ID", example = "1")
    val id: Long,
    @field:Schema(description = "시나리오 이름", example = "순찰 시나리오 1")
    val scenarioName: String,
    @field:Schema(description = "시설 이름", example = "중앙 건물")
    val facilityName: String,
    @field:Schema(description = "트리거 소스", example = "MANUAL")
    val triggerType: TriggerSource,
    @field:Schema(description = "실행 상태", example = "COMPLETED")
    val executionStatus: ScenarioExecutionStatus,
    @field:Schema(description = "트리거된 시간", example = "2025-02-04T10:00:00")
    val triggeredAt: LocalDateTime?,
    @field:Schema(description = "시작 시간", example = "2025-02-04T10:00:01")
    val startedAt: LocalDateTime?,
    @field:Schema(description = "종료 시간", example = "2025-02-04T10:05:00")
    val finishedAt: LocalDateTime?,
    @field:Schema(description = "오류 메시지", example = "연결 타임아웃")
    val errorMessage: String?,
    @field:Schema(description = "생성자", example = "admin@example.com")
    val createBy: String?,
)

@Schema(description = "시나리오 실행 상세 응답")
data class ScenarioExecutionDetailResponse(
    @field:Schema(description = "시나리오 실행 정보")
    val execution: ScenarioExecutionResponse,
    @field:Schema(description = "씬 실행 목록")
    val sceneExecutions: List<SceneExecutionResponse>,
)

@Schema(description = "씬 실행 응답")
data class SceneExecutionResponse(
    @field:Schema(description = "씬 실행 ID", example = "1")
    val id: Long,
    @field:Schema(description = "씬 이름", example = "오전 순찰 모드")
    val sceneName: String,
    @field:Schema(description = "실행 순서 (1부터 시작)", example = "1")
    val executionOrder: Int,
    @field:Schema(description = "재생 시간 (초)", example = "10")
    val duration: Int,
    @field:Schema(description = "좌표 정보")
    val position: Spatial?,
    @field:Schema(description = "회전 정보")
    val rotation: Spatial?,
    @field:Schema(description = "씬의 기기 액션 실행 목록")
    val sceneDeviceActionExecutionResponse: List<SceneDeviceActionExecutionResponse>,
)

@Schema(description = "기기 액션 실행 응답")
data class SceneDeviceActionExecutionResponse(
    @field:Schema(description = "기기 액션 실행 ID", example = "1")
    val id: Long,
    @field:Schema(description = "기기 고유 식별자", example = "DEV-001")
    val deviceId: String,
    @field:Schema(description = "기기 타입", example = "CAMERA")
    val deviceType: DeviceType,
    @field:Schema(description = "실행된 액션", example = "START")
    val deviceAction: DeviceAction,
    @field:Schema(description = "액션 파라미터", example = "pan_left")
    val actionParam: String?,
    @field:Schema(description = "실행 순서", example = "1")
    val executionOrder: Int?,
)

@Schema(description = "시나리오 실행 실패 요청")
data class ScenarioExecutionFailRequest(
    @field:Schema(description = "오류 메시지", example = "장치 연결 실패")
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
