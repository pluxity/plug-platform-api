package com.pluxity.facility.dto

import com.pluxity.facility.floor.dto.FloorRequest
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@JvmRecord
data class FacilityFloorUpdateRequest(
    @field:Schema(
        description = "층 정보",
        requiredMode = Schema.RequiredMode.REQUIRED
    ) @param:Schema(description = "층 정보", requiredMode = Schema.RequiredMode.REQUIRED) val floors: @NotNull MutableList<FloorRequest?>?
)
