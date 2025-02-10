package com.pluxity.user.dto;

import jakarta.validation.constraints.NotBlank;

public record PermissionCreateRequest(
        @NotBlank(message = "Permission name cannot be empty") String name, String description) {}
