package com.pluxity.user.dto;

import jakarta.validation.constraints.NotBlank;

public record RequestRole(
        @NotBlank(message = "Role name must not be blank") String roleName) {}
