package com.pluxity.user.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleCreateRequest(@NotBlank(message = "Role name cannot be empty") String roleName) {}
