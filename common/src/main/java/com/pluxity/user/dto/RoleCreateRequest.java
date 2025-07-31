package com.pluxity.user.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record RoleCreateRequest(
        @NotBlank(message = "Role name cannot be empty") String name,
        String description,
        List<Long> permissionGroupIds) {}
