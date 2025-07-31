package com.pluxity.user.dto;

import com.pluxity.permission.dto.PermissionResponse;
import com.pluxity.user.entity.Role;
import java.util.Collections;
import java.util.List;

public record RoleResponse(
        Long id, String name, String description, List<PermissionResponse> permissions) {
    public static RoleResponse from(Role role) {
        if (role == null) {
            return null;
        }

        List<PermissionResponse> permissionResponses;
        if (role.getRolePermissions() != null) {
            permissionResponses =
                    role.getRolePermissions().stream()
                            .flatMap(rp -> rp.getPermissionGroup().getPermissions().stream())
                            .map(PermissionResponse::from)
                            .toList();
        } else {
            permissionResponses = Collections.emptyList();
        }

        return new RoleResponse(
                role.getId(), role.getName(), role.getDescription(), permissionResponses);
    }
}
