package com.pluxity.user.dto;

import com.pluxity.user.entity.User;
import java.util.List;

public record UserResponse(
        Long id, String username, String name, String code, List<RoleResponse> roles) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getCode(),
                user.getRoles().stream().map(RoleResponse::from).toList());
    }
}
