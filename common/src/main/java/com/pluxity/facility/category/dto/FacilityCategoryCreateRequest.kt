package com.pluxity.facility.category.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class FacilityCategoryCreateRequest(
    @Schema(description = "카테고리 이름", example = "시설 카테고리")
    @field:Size(max = 50, message = "카테고리 이름은 50 초과할 수 없습니다")
    @field:NotBlank
    val name: String,
    @Schema(description = "부모 카테고리 ID", example = "1")
    val parentId: Long? = null,
)
