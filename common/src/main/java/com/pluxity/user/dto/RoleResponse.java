package com.pluxity.user.dto;

import com.pluxity.user.entity.Role;
import java.util.List;

public record RoleResponse(Long id, String name) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName());
    }
}
