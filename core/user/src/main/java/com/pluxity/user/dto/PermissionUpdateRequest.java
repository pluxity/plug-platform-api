package com.pluxity.user.dto;

import jakarta.validation.constraints.NotBlank;

public record PermissionUpdateRequest(
        @NotBlank(message = "Permission name cannot be empty") String name, String description) {}
