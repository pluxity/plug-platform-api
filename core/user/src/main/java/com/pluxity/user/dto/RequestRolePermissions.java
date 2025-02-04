package com.pluxity.user.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RequestRolePermissions(
        @NotEmpty(message = "Permission IDs cannot not be empty") List<Long> permissionIds) {}
