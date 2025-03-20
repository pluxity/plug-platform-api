package com.pluxity.building.dto;

public record BuildingPermissionCreateRequest(
        Long roleId,
        Long buildingId
) {
} 