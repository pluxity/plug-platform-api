package com.pluxity.user.dto;

import com.pluxity.user.entity.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PermissionRequest(
        @NotNull @Schema(description = "권한을 부여/관리할 역할(Role)의 ID", example = "1") Long roleId,
        @NotNull @Schema(description = "권한을 관리할 리소스의 타입", example = "FACILITY")
                ResourceType resourceName,
        @NotEmpty @Schema(description = "권한을 부여/관리할 리소스들의 ID 목록", example = "[101, 102, 105]")
                List<Long> resourceId) {}
