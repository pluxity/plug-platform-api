package com.pluxity.facility.dto;

import lombok.Builder;

@Builder
public record PanoramaResponse(
        FacilityResponse facility,
        String address,
        Double latitude,
        Double longitude
) {
}
