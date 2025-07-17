package com.pluxity.facility.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record FacilityPathSaveRequest(
        @Schema(description = "이름", example = "이름", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull
                String name,
        @Schema(
                        description = "타입",
                        example = "SUBWAY(\"지하철\"), WAY(\"길찾기\"), PATROL(\"순찰\")",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull
                String type,
        @Schema(
                        description = "경로정보",
                        example = "[{\"id\"...}]",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull
                String path) {}
