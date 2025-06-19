package com.pluxity.facility.building.dto;

import com.pluxity.facility.facility.dto.FacilityUpdateRequest; // Ensure import
import com.pluxity.facility.floor.dto.FloorRequest; // If floors can be updated too
import jakarta.validation.Valid; // Added
import java.util.List;

public record BuildingUpdateRequest(
    @Valid FacilityUpdateRequest facility, // To update composed facility
    List<@Valid FloorRequest> floors      // To update floors
) {}
