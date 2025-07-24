package com.pluxity.user.dto;

import com.pluxity.user.entity.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PermissionCreateRequest(
        @NotNull @Schema(description = "권한을 관리할 리소스의 타입", example = "FACILITY")
                ResourceType resourceName,
        @NotBlank @Schema(description = "권한을 부여/관리할 리소스의 ID", example = "101") String resourceId) {}
