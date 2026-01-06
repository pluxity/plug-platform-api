package com.pluxity.asset.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AssetCategoryUpdateRequest(
    @field:Schema(description = "카테고리 이름", example = "그래픽 에셋")
    @field:NotBlank(message = "카테고리 이름은 필수입니다")
    @field:Size(max = 50, message = "카테고리 이름은 50자를 초과할 수 없습니다")
    val name: String,
    @field:Schema(description = "카테고리 코드", example = "GRAPHIC_ASSET")
    @field:NotBlank(message = "카테고리 코드는 필수입니다")
    @field:Size(max = 50, message = "카테고리 코드는 50글자를 초과할 수 없습니다")
    val code: String,
    @field:Schema(description = "부모 카테고리 ID", example = "1")
    val parentId: Long? = null,
    @field:Schema(description = "아이콘 파일 ID", example = "10")
    val thumbnailFileId: Long? = null,
)
