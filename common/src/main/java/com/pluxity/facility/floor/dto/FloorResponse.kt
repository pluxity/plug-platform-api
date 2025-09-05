package com.pluxity.facility.floor.dto

import com.pluxity.facility.floor.Floor

@JvmRecord
data class FloorResponse(val name: String?, val floorId: String?) {
    companion object {
        fun from(floor: Floor): FloorResponse {
            return FloorResponse(floor.getName(), floor.getFloorId())
        }
    }
}
