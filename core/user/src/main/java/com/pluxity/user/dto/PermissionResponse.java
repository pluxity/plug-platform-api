package com.pluxity.user.dto;

import com.pluxity.user.entity.Permission;

public record PermissionResponse(Long id, String name, String description) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
                permission.getId(), permission.getName(), permission.getDescription());
    }
}
