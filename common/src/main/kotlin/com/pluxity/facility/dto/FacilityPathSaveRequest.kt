package com.pluxity.facility.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

data class FacilityPathSaveRequest(
    @field:Schema(description = "이름", example = "이름", requiredMode = Schema.RequiredMode.REQUIRED)
    @field:NotNull
    var name: String,
    @field:Schema(
        description = "타입(SUBWAY: 지하철, WAY: 길찾기, PATROL: 순찰)",
        allowableValues = ["SUBWAY", "WAY", "PATROL"],
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    @field:NotNull
    var type: String,
    @field:Schema(
        description = "경로정보",
        example = "[{\"id\"...}]",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    @field:NotNull
    var path: String,
)
