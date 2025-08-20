package com.pluxity.station.dto

data class StationInfoResponse(
    val lineIds: List<Long>,
    val stationCodes: List<String>,
)
