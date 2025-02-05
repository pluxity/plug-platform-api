package com.pluxity.user.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record UserRoleAssignRequest(
        @NotBlank(message = "role IDs cannot be empty") List<Long> roleIds) {}
