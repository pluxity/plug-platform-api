package com.pluxity.park.dto

import com.pluxity.facility.dto.FacilityResponse

data class ParkResponse(
    val facility: FacilityResponse,
    val boundary: String?,
)
