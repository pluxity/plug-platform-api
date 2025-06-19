package com.pluxity.facility.building.dto;

import com.pluxity.facility.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.floor.dto.FloorRequest;
import jakarta.validation.Valid; // Added
import jakarta.validation.constraints.NotNull; // Added
import java.util.List;

public record BuildingCreateRequest(
    @NotNull @Valid FacilityCreateRequest facility,
    List<@Valid FloorRequest> floors
) {}
