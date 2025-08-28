package com.pluxity.device.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class DeviceCategoryRequest(
    @field:Schema(description = "카테고리 이름", example = "카테고리")
    @field:Size(max = 50, message = "카테고리 이름은 50자 이하여야 합니다.")
    @field:NotBlank
    val name: String,
    @field:Schema(description = "부모 카테고리 ID", example = "1")
    val parentId: Long?,
    @field:Schema(description = "아이콘 파일 ID", example = "1")
    val thumbnailFileId: Long?,
)
