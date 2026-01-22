package com.pluxity.patrol.dto

import com.pluxity.feature.entity.Spatial
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

data class SceneCreateRequest(
    @field:NotBlank(message = "씬 이름은 필수입니다")
    @field:Size(max = 100, message = "씬 이름은 100자를 초과할 수 없습니다")
    @field:Schema(description = "씬 이름", example = "오전 순찰 모드")
    val name: String,
    @field:Size(max = 500, message = "설명은 500자를 초과할 수 없습니다")
    @field:Schema(description = "설명", example = "오전 9시 자동으로 실행되는 순찰 씬")
    val description: String?,
    @field:PositiveOrZero(message = "유지 시간은 0보다 작을 수 없습니다")
    @field:Schema(description = "유지 시간 (초 단위)", minimum = "0.0", example = "10.5")
    @field:NotBlank(message = "유지 시간은 필수입니다.")
    val duration: Double,
    @field:Schema(description = "좌표 정보 (위치)")
    val position: Spatial?,
    @field:Schema(description = "회전 정보")
    val rotation: Spatial?,
    @field:Valid
    @field:Schema(description = "씬에 포함된 기기 액션 목록")
    val sceneDeviceActionRequests: List<SceneDeviceActionCreateRequest>?,
)
