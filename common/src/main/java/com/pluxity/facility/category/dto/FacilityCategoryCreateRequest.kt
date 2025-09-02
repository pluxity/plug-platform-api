package com.pluxity.facility.category.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@JvmRecord
data class FacilityCategoryCreateRequest(
    @field:Schema(description = "카테고리 이름", example = "시설 카테고리") @param:Schema(
        description = "카테고리 이름",
        example = "시설 카테고리"
    ) val name: @Size(max = 50, message = "카테고리 이름은 50 초과할 수 없습니다") @NotBlank String?,
    @field:Schema(description = "부모 카테고리 ID", example = "1") @param:Schema(
        description = "부모 카테고리 ID",
        example = "1"
    ) val parentId: Long?
)
