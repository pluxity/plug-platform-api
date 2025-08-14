package com.pluxity.device.dto

import io.swagger.v3.oas.annotations.media.Schema

data class GsDeviceCctvUpdateRequest(
    @field:Schema(description = "CCTV ID 목록")
    val cctvIds: MutableList<String> = mutableListOf(),
)
