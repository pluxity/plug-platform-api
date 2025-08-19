package com.pluxity.station.dto

import com.pluxity.facility.dto.FacilityResponse
import com.pluxity.facility.floor.dto.FloorResponse

data class StationResponse(
    val facility: FacilityResponse,
    val floors: List<FloorResponse>,
    val stationInfo: StationInfoResponse,
)
