package com.pluxity.park.dto;

import com.pluxity.facility.dto.FacilityUpdateRequest;
import io.swagger.v3.oas.annotations.media.Schema;

public record ParkUpdateRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) FacilityUpdateRequest facility,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String boundary) {}
