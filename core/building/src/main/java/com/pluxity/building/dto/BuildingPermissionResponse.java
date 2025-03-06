package com.pluxity.building.dto;

import com.pluxity.building.entity.BuildingPermission;

public record BuildingPermissionResponse(
        Long id,
        Long roleId,
        String roleName,
        Long buildingId
) {
    public static BuildingPermissionResponse from(BuildingPermission permission) {
        return new BuildingPermissionResponse(
                permission.getId(),
                permission.getRole().getId(),
                permission.getRole().getRoleName(),
                permission.getResourceId()
        );
    }
} 