package com.pluxity.facility.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.pluxity.facility.Facility
import com.pluxity.facility.FacilityType
import com.pluxity.file.dto.FileResponse
import com.pluxity.global.response.BaseResponse

data class FacilityResponse(
    val id: Long?,
    val code: String?,
    val name: String,
    val description: String?,
    val type: FacilityType?,
    val drawing: FileResponse,
    val thumbnail: FileResponse,
    val paths: List<FacilityPathResponse>,
    val lon: Double?,
    val lat: Double?,
    val locationMeta: String?,
    @JsonUnwrapped val baseResponse: BaseResponse,
)

fun Facility.toResponse(
    drawing: FileResponse? = null,
    thumbnail: FileResponse? = null,
): FacilityResponse {
    val emptyFileResponse = FileResponse(null, null, null, null, null, null)

    return FacilityResponse(
        id = this.id,
        code = this.code,
        name = this.name,
        description = this.description,
        type = this.facilityType,
        drawing = drawing ?: emptyFileResponse,
        thumbnail = thumbnail ?: emptyFileResponse,
        paths = this.paths.map { FacilityPathResponse.from(it) },
        lon = this.position?.lon,
        lat = this.position?.lat,
        locationMeta = this.position?.locationMeta,
        baseResponse = BaseResponse.of(this),
    )
}
