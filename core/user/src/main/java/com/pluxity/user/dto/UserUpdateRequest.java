package com.pluxity.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserUpdateRequest(
        @NotBlank(message = "Username cannot be empty") String username,
        @NotBlank(message = "Password cannot be empty") String password,
        @NotBlank(message = "Name cannot be empty") String name,
        @NotBlank(message = "Code cannot be empty") String code,
        Long templateId
        ) {}
