package com.pluxity.patrol.dto

import com.pluxity.patrol.constant.DeviceAction
import com.pluxity.patrol.constant.DeviceType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero

data class SceneDeviceActionRequest(
    @field:Schema(
        description = "scene_device_action ID (기존 데이터 수정 시 ID 필수, 신규 생성 시 null)",
        example = "null",
    )
    val sceneDeviceActionId: Long? = null,
    @field:NotBlank(message = "기기 ID는 필수입니다")
    @field:Schema(description = "기기 고유 식별자", example = "DEV-001")
    val deviceId: String,
    @field:NotNull(message = "장치 액션 정의는 필수입니다")
    @field:Schema(description = "장치 액션 (Enum)")
    var deviceAction: DeviceAction,
    @field:NotNull(message = "장치 타입은 필수입니다")
    @field:Schema(description = "장치 타입 (Enum)")
    var deviceType: DeviceType,
    @field:Schema(description = "동작 파라미터")
    var actionParam: String?,
    @field:PositiveOrZero(message = "실행 순서는 0 이상이어야 합니다")
    @field:Schema(description = "실행 순서", defaultValue = "0", minimum = "0")
    var executionOrder: Int? = 0,
)
