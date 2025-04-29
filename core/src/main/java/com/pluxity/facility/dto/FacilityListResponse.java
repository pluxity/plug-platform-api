package com.pluxity.facility.dto;

import lombok.Builder;

@Builder
public record FacilityListResponse(
        Long id,
        String name,
        String address,
        String createdAt
) {
}
