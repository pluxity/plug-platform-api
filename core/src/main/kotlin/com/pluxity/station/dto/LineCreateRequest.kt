package com.pluxity.station.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LineCreateRequest(
    @field:Schema(
        description = "노선 이름",
        example = "1호선",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    @field:NotBlank(message = "노선 이름은 필수입니다")
    @field:Size(max = 50, message = "노선 이름은 50자 이하여야 합니다")
    val name: String,
    val color: String?,
)
