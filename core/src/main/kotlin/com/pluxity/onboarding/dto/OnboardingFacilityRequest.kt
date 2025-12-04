package com.pluxity.onboarding.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class OnboardingFacilityRequest(
    @field:Schema(description = "시설 이름", example = "서울역", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotBlank(message = "이름은 필수 입니다.")
    @field:Size(max = 50, message = "이름은 최대 50자까지 입력 가능합니다.")
    val name: String,
    @field:Schema(description = "시설 설명", example = "description")
    @field:Size(max = 255, message = "시설 설명은 최대 255자까지 입력 가능합니다.")
    val description: String?,
    @field:Schema(description = "썸네일파일 ID", example = "1")
    val thumbnailId: Long?,
)
