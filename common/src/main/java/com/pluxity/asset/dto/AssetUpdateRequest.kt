package com.pluxity.asset.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@JvmRecord
data class AssetUpdateRequest(
    @field:Schema(description = "에셋 이름", example = "로고 이미지") @param:Schema(
        description = "에셋 이름",
        example = "로고 이미지"
    ) val name: @Size(max = 50, message = "에셋 이름은 50자를 초과할 수 없습니다") @NotBlank(message = "에셋 이름은 필수입니다") String?,
    @field:Schema(description = "에셋 코드", example = "LOGO_IMAGE") @param:Schema(
        description = "에셋 코드",
        example = "LOGO_IMAGE"
    ) val code: @Size(max = 50, message = "에셋 코드는 50자를 초과할 수 없습니다") @NotBlank(message = "에셋 코드는 필수입니다") String?,
    @field:Schema(description = "파일 ID", example = "1") @param:Schema(
        description = "파일 ID",
        example = "1"
    ) val fileId: Long?,
    @field:Schema(description = "썸네일 파일 ID", example = "2") @param:Schema(
        description = "썸네일 파일 ID",
        example = "2"
    ) val thumbnailFileId: Long?,
    @field:Schema(description = "카테고리 ID", example = "3") @param:Schema(
        description = "카테고리 ID",
        example = "3"
    ) val categoryId: Long?
)
