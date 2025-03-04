package com.pluxity.building.dto;

import lombok.Builder;

@Builder
public record BuildingCreateRequest(
        String name,
        Long fileId
) {
}
