package com.pluxity.facility.dto

import com.pluxity.facility.path.FacilityPath

data class FacilityPathResponse(
    val id: Long,
    val name: String,
    val type: String,
    val path: String,
)

fun FacilityPath.toPathResponse(): FacilityPathResponse =
    FacilityPathResponse(
        id = this.requiredId(),
        name = this.name,
        type = this.pathType.name,
        path = this.path,
    )
