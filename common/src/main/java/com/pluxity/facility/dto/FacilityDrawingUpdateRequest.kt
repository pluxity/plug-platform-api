package com.pluxity.facility.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@JvmRecord
data class FacilityDrawingUpdateRequest(
    @field:Schema(
        description = "도면 파일 ID",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    ) @param:Schema(
        description = "도면 파일 ID",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    ) val drawingFileId: @NotNull Long?,
    @field:Schema(description = "변경사항", example = "변경사항") @param:Schema(
        description = "변경사항",
        example = "변경사항"
    ) val comment: String?
)
