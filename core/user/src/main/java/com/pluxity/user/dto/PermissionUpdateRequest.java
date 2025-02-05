package com.pluxity.user.dto;

import jakarta.validation.constraints.NotBlank;

public record PermissionUpdateRequest(
        @NotBlank(message = "Permission description cannot be empty") String description) {}
