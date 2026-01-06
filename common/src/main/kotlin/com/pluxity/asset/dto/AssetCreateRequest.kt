package com.pluxity.asset.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AssetCreateRequest(
    @field:Schema(description = "에셋 이름", example = "로고 이미지")
    @field:NotBlank(message = "에셋 이름은 필수입니다")
    @field:Size(max = 100, message = "에셋 이름은 100자를 초과할 수 없습니다")
    val name: String,
    @field:Schema(description = "에셋 코드", example = "LOGO_IMAGE")
    @field:NotBlank(message = "에셋 코드는 필수입니다")
    @field:Size(max = 50, message = "에셋 코드는 50글자를 초과할 수 없습니다")
    val code: String,
    @field:Schema(description = "파일 ID", example = "1")
    val fileId: Long? = null,
    @field:Schema(description = "썸네일 파일 ID", example = "2")
    val thumbnailFileId: Long? = null,
    @field:Schema(description = "카테고리 ID", example = "3")
    val categoryId: Long? = null,
)
