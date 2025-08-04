package com.pluxity.building.dto;

import com.pluxity.facility.dto.FacilityUpdateRequest;
import com.pluxity.facility.floor.dto.FloorRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record BuildingUpdateRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) FacilityUpdateRequest facility,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<FloorRequest> floors) {}
