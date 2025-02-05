package com.pluxity.user.dto;

import com.pluxity.user.entity.Permission;
import java.util.List;

public record PermissionResponse(Long id, String description, List<RoleResponse> roles) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getDescription(),
                permission.getRolePermissions().stream()
                        .map(rolePermission -> RoleResponse.from(rolePermission.getRole()))
                        .toList());
    }
}
