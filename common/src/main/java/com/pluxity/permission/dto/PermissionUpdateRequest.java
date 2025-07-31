package com.pluxity.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PermissionUpdateRequest(
        @Schema(
                        description = "변경할 리소스의 타입",
                        example = "FACILITY",
                        allowableValues = {"FACILITY", "BUILDING", "PARK", "DEVICE"})
                String resourceName,
        @Schema(description = "변경할 리소스의 ID", example = "102") String resourceId) {}
