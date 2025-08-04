package com.pluxity.facility.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FacilityLocationUpdateRequest(
        @Schema(description = "경도", example = "127", requiredMode = Schema.RequiredMode.REQUIRED)
                Double lon,
        @Schema(description = "위도", example = "37", requiredMode = Schema.RequiredMode.REQUIRED)
                Double lat,
        @Schema(
                        description = "위치 관련 부가정보",
                        example = "[{\"height\":..}]]",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String locationMeta) {}
