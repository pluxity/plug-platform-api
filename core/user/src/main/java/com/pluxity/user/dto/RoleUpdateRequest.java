package com.pluxity.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RoleUpdateRequest(@NotBlank(message = "Role name cannot be empty") String roleName) {}
