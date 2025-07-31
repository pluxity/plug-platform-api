package com.pluxity.permission.dto;

import com.pluxity.permission.Permission;

public record PermissionResponse(Long id, String resourceName, String resourceId) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
                permission.getId(), permission.getResourceName(), permission.getResourceId());
    }
}
