package com.pluxity.facility.dto

import com.pluxity.facility.floor.dto.FloorRequest
import io.swagger.v3.oas.annotations.media.Schema

data class FacilityFloorUpdateRequest(
    @field:Schema(description = "층 정보", requiredMode = Schema.RequiredMode.REQUIRED)
    val floors: List<FloorRequest>,
)
