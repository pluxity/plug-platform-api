package com.pluxity.station.dto

import com.pluxity.facility.dto.FacilityUpdateRequest
import com.pluxity.facility.floor.dto.FloorRequest
import io.swagger.v3.oas.annotations.media.Schema

data class StationUpdateRequest(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    val facility: FacilityUpdateRequest,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    val floors: List<FloorRequest> = emptyList(),
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    val stationInfo: StationUpdateInfo? = null,
)
