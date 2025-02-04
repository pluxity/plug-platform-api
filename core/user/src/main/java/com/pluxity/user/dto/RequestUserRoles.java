package com.pluxity.user.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record RequestUserRoles(
        @NotBlank(message = "role IDs cannot be empty") List<Long> roleIds) {}
