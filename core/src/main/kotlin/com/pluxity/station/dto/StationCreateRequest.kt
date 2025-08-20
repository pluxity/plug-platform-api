package com.pluxity.station.dto

import com.pluxity.facility.dto.FacilityCreateRequest
import com.pluxity.facility.floor.dto.FloorRequest

data class StationCreateRequest(
    val facility: FacilityCreateRequest,
    val floors: List<FloorRequest> = emptyList(),
    val lineIds: List<Long> = emptyList(),
    val stationCodes: List<String> = emptyList(),
)
