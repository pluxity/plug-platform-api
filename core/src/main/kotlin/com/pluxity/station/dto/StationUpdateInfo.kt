package com.pluxity.station.dto

import io.swagger.v3.oas.annotations.media.Schema

data class StationUpdateInfo(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    val lineIds: List<Long>,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    val stationCodes: List<String>,
)
