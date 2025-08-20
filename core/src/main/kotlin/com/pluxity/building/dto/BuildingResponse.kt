package com.pluxity.building.dto

import com.pluxity.facility.dto.FacilityResponse
import com.pluxity.facility.floor.dto.FloorResponse

data class BuildingResponse(
    val facility: FacilityResponse,
    val floors: List<FloorResponse?>?,
)
