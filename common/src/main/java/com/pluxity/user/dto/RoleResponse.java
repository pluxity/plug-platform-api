package com.pluxity.user.dto;

import com.pluxity.user.entity.Role;

public record RoleResponse(Long id, String name, String description) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(role.getId(), role.getName(), role.getDescription());
    }
}
