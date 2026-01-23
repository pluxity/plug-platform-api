package com.pluxity.patrol.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

@Schema(description = "시나리오-씬 생성 요청")
data class ScenarioSceneCreateRequest(
    @field:NotNull(message = "씬 ID는 필수입니다.")
    @field:Schema(description = "씬 ID", example = "1")
    val sceneId: Long,
    @field:Min(value = 1, message = "실행 순서는 1 이상이어야 합니다.")
    @field:Schema(description = "실행 순서 (1부터 시작)", example = "1")
    val order: Int,
    @field:Schema(description = "재생 시간 (초), null이면 씬의 기본 duration 사용", example = "10.0")
    val duration: Double? = null,
    @field:Schema(description = "전환 시간 (초)", example = "10.0")
    val transitionTime: Double,
)
