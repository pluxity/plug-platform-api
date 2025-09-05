package com.pluxity.facility.dto

import com.pluxity.facility.path.FacilityPath

@JvmRecord
data class FacilityPathResponse(val id: Long?, val name: String?, val type: String?, val path: String?) {
    companion object {
        fun from(facilityPath: FacilityPath): FacilityPathResponse {
            return FacilityPathResponse(
                facilityPath.getId(),
                facilityPath.getName(),
                facilityPath.getPathType().name,
                facilityPath.getPath()
            )
        }
    }
}
