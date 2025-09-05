package com.pluxity.facility.floor.dto

import com.pluxity.facility.floor.Floor

data class FloorResponse(
    val name: String,
    val floorId: String,
)

fun Floor.toFloorResponse(): FloorResponse =
    FloorResponse(
        name = this.name,
        floorId = this.floorId,
    )
