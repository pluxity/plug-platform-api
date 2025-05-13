package com.pluxity.facility.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record StationResponse(
        FacilityResponse facility,
        List<FloorResponse> floors
) {
}
