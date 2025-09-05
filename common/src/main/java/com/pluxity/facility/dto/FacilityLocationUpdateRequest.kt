package com.pluxity.facility.dto

import io.swagger.v3.oas.annotations.media.Schema

@JvmRecord
data class FacilityLocationUpdateRequest(
    @field:Schema(
        description = "경도",
        example = "127",
        requiredMode = Schema.RequiredMode.REQUIRED
    ) @param:Schema(
        description = "경도",
        example = "127",
        requiredMode = Schema.RequiredMode.REQUIRED
    ) val lon: Double?,
    @field:Schema(
        description = "위도",
        example = "37",
        requiredMode = Schema.RequiredMode.REQUIRED
    ) @param:Schema(
        description = "위도",
        example = "37",
        requiredMode = Schema.RequiredMode.REQUIRED
    ) val lat: Double?,
    @field:Schema(
        description = "위치 관련 부가정보",
        example = "[{\"height\":..}]]",
        requiredMode = Schema.RequiredMode.REQUIRED
    ) @param:Schema(
        description = "위치 관련 부가정보",
        example = "[{\"height\":..}]]",
        requiredMode = Schema.RequiredMode.REQUIRED
    ) val locationMeta: String?
)
