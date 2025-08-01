package com.pluxity.facility.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FacilityPathUpdateRequest(
        @Schema(description = "이름", example = "이름") String name,
        @Schema(description = "타입", example = "SUBWAY(\"지하철\"), WAY(\"길찾기\"), PATROL(\"순찰\")")
                String type,
        @Schema(description = "경로정보", example = "[{\"id\"...}]") String path) {}
