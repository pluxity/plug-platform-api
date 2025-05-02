package com.pluxity.facility.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record BuildingResponse(
        FacilityResponse facility,
        String address,
        Double latitude,
        Double longitude,
        List<FloorResponse> floors
) {

}
