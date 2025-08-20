package com.pluxity.park.dto

import com.pluxity.facility.dto.FacilityCreateRequest

data class ParkCreateRequest(
    val facility: FacilityCreateRequest,
    val boundary: String?,
)
