package com.pluxity.user.dto;

import com.pluxity.user.entity.Role;
import java.util.List;

public record RoleResponse(Long id, String roleName, List<PermissionInfo> permissions) {
    public record PermissionInfo(Long id, String roleName, String description) {}

    public static RoleResponse from(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getRoleName(),
                role.getRolePermissions().stream()
                        .map(
                                rolePermission ->
                                        new PermissionInfo(
                                                rolePermission.getPermission().getId(),
                                                rolePermission.getPermission().getName(),
                                                rolePermission.getPermission().getDescription()))
                        .toList());
    }
}
