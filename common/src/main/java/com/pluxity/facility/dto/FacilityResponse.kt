package com.pluxity.facility.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.pluxity.facility.Facility
import com.pluxity.facility.FacilityType
import com.pluxity.facility.path.FacilityPath
import com.pluxity.file.dto.FileResponse
import com.pluxity.global.response.BaseResponse
import lombok.Builder

@Builder
@JvmRecord
data class FacilityResponse(
    val id: Long?,
    val code: String?,
    val name: String?,
    val description: String?,
    val type: FacilityType?,
    val drawing: FileResponse?,
    val thumbnail: FileResponse?,
    val paths: MutableList<FacilityPathResponse?>?,
    val lon: Double?,
    val lat: Double?,
    val locationMeta: String?,
    @field:JsonUnwrapped @param:JsonUnwrapped val baseResponse: BaseResponse?
) {
    companion object {
        @JvmStatic
        fun from(
            facility: Facility, drawing: FileResponse?, thumbnail: FileResponse?
        ): FacilityResponse {
            return FacilityResponse(
                facility.getId(),
                facility.getCode(),
                facility.getName(),
                facility.getDescription(),
                facility.getFacilityType(),
                if (drawing != null) drawing else FileResponse(null, null, null, null, null, null),
                if (thumbnail != null) thumbnail else FileResponse(null, null, null, null, null, null),
                facility.getPaths().stream().map<FacilityPathResponse?> { facilityPath: FacilityPath? -> FacilityPathResponse.from(facilityPath) }.toList(),
                if (facility.getPosition() != null) facility.getPosition().getLon() else null,
                if (facility.getPosition() != null) facility.getPosition().getLat() else null,
                if (facility.getPosition() != null) facility.getPosition().getLocationMeta() else null,
                BaseResponse.of(facility)
            )
        }
    }
}
