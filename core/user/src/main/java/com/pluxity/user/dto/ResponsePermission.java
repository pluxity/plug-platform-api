package com.pluxity.user.dto;

import com.pluxity.user.entity.Permission;
import java.util.List;

public record ResponsePermission(Long id, String description, List<ResponseRole> roles) {
    public static ResponsePermission from(Permission permission) {
        return new ResponsePermission(
                permission.getId(),
                permission.getDescription(),
                permission.getRolePermissions().stream()
                        .map(rolePermission -> ResponseRole.from(rolePermission.getRole()))
                        .toList());
    }
}
