package com.pluxity.park.dto

import com.pluxity.facility.dto.FacilityUpdateRequest
import io.swagger.v3.oas.annotations.media.Schema

data class ParkUpdateRequest(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    val facility: FacilityUpdateRequest,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    val boundary: String,
)
