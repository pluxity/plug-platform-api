package com.pluxity.station.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.pluxity.global.response.BaseResponse
import com.pluxity.global.response.toBaseResponse
import com.pluxity.station.Line

data class LineResponse(
    val id: Long?,
    val color: String?,
    val name: String,
    val stationIds: List<Long>,
    @field:JsonUnwrapped val baseResponse: BaseResponse,
)

fun Line.toLineResponse(): LineResponse =
    LineResponse(
        this.id,
        this.color,
        this.name,
        this.getStations().mapNotNull { it.id },
        this.toBaseResponse(),
    )
