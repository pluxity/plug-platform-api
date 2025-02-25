package com.pluxity.building.dto;

import lombok.Builder;

@Builder
public record BuildingListResponse(
        Long id,
        String name,
        String address,
        String createdAt
) {
}
