package com.pluxity.user.dto;

import jakarta.validation.constraints.NotBlank;

public record RequestPermission(
        @NotBlank(message = "Description must not be blank") String description) {}
