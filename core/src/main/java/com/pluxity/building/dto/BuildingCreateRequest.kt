package com.pluxity.building.dto

import com.pluxity.facility.dto.FacilityCreateRequest
import com.pluxity.facility.floor.dto.FloorRequest

data class BuildingCreateRequest(
    val facility: FacilityCreateRequest,
    val floors: List<FloorRequest>,
)
