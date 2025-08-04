package com.pluxity.facility.dto;

import com.pluxity.facility.floor.dto.FloorRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record FacilityFloorUpdateRequest(
        @Schema(description = "층 정보", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull
                List<FloorRequest> floors) {}
