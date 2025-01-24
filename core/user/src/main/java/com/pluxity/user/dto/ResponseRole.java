package com.pluxity.user.dto;

import com.pluxity.user.entity.Role;
import java.util.List;

public record ResponseRole(Long id, String roleName, List<PermissionInfo> permissions) {
    public record PermissionInfo(Long id, String description) {}

    public static ResponseRole from(Role role) {
        return new ResponseRole(
                role.getId(),
                role.getRoleName(),
                role.getRolePermissions().stream()
                        .map(
                                rolePermission ->
                                        new PermissionInfo(
                                                rolePermission.getPermission().getId(),
                                                rolePermission.getPermission().getDescription()))
                        .toList());
    }
}
