package com.pluxity.facility.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;

public record BuildingCreateRequest(
        FacilityCreateRequest facility,
        String address,
        Double latitude,
        Double longitude,
        List<FloorRequest> floors
) {
}
