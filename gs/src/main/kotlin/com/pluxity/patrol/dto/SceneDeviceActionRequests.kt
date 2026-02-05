package com.pluxity.patrol.dto

import com.pluxity.patrol.constant.DeviceAction
import com.pluxity.patrol.constant.DeviceType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero

@Schema(description = "씬 기기 액션 생성 요청")
data class SceneDeviceActionCreateRequest(
    @field:NotBlank(message = "기기 ID는 필수입니다")
    @field:Schema(description = "기기 고유 식별자", example = "DEV-001", required = true)
    val deviceId: String,
    @field:Schema(description = "장치 액션 (Enum: START, STOP, ROTATE, etc.)", required = true)
    val deviceAction: DeviceAction,
    @field:Schema(description = "장치 타입 (Enum: CAMERA, LIGHT, MOTION_SENSOR, etc.)", required = true)
    val deviceType: DeviceType,
    @field:Schema(description = "동작 파라미터 (JSON 또는 자유 형식)", example = "pan_left")
    val actionParam: String?,
    @field:PositiveOrZero(message = "실행 순서는 0 이상이어야 합니다")
    @field:Schema(description = "실행 순서 (여러 액션의 실행 순서)", defaultValue = "0", minimum = "0")
    val executionOrder: Int? = 0,
)

@Schema(description = "씬 기기 액션 수정 요청")
data class SceneDeviceActionUpdateRequest(
    @field:Schema(
        description = "scene_device_action ID (기존 데이터 수정 시 ID 필수, 신규 생성 시 null)",
        example = "1",
    )
    val sceneDeviceActionId: Long? = null,
    @field:NotBlank(message = "기기 ID는 필수입니다")
    @field:Schema(description = "기기 고유 식별자", example = "DEV-001", required = true)
    val deviceId: String,
    @field:Schema(description = "장치 액션 (Enum: START, STOP, ROTATE, etc.)", required = true)
    val deviceAction: DeviceAction,
    @field:Schema(description = "장치 타입 (Enum: CAMERA, LIGHT, MOTION_SENSOR, etc.)", required = true)
    val deviceType: DeviceType,
    @field:Schema(description = "동작 파라미터 (JSON 또는 자유 형식)", example = "pan_left")
    val actionParam: String?,
    @field:PositiveOrZero(message = "실행 순서는 0 이상이어야 합니다")
    @field:Schema(description = "실행 순서 (여러 액션의 실행 순서)", defaultValue = "0", minimum = "0")
    val executionOrder: Int? = 0,
)
