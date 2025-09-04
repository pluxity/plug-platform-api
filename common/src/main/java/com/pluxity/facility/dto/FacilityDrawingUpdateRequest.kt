package com.pluxity.facility.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

data class FacilityDrawingUpdateRequest(
    @Schema(description = "도면 파일 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotNull
    val drawingFileId: Long,
    @Schema(description = "변경사항", example = "변경사항")
    val comment: String?,
)
