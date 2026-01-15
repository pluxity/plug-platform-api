package com.pluxity.patrol.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "시나리오 수정 요청")
data class ScenarioUpdateRequest(
    @field:NotBlank(message = "시나리오 이름은 필수입니다.")
    @field:Size(max = 100, message = "시나리오 이름은 100자를 초과할 수 없습니다")
    @field:Schema(description = "시나리오 이름", example = "수정된 시나리오")
    val name: String,
    @field:Size(max = 500, message = "설명은 500자를 초과할 수 없습니다")
    @field:Schema(description = "시나리오 설명", example = "수정된 설명")
    val description: String?,
    @field:Schema(description = "활성화 여부", example = "true")
    val isActive: Boolean?,
    @field:Valid
    @field:Schema(description = "시나리오에 포함된 씬 목록")
    val scenarioScenes: List<ScenarioSceneUpdateRequest>? = null,
    @field:Valid
    @field:Schema(description = "트리거 목록 (null이면 기존 유지, 빈 리스트면 전체 삭제)")
    val triggers: List<TriggerRequest>? = null,
)
