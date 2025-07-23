package com.pluxity.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PermissionResponse(
        @Schema(description = "권한을 관리할 리소스의 타입", example = "FACILITY") String resourceName,
        @Schema(description = "해당 리소스 타입에 대해 부여된 리소스 ID 목록", example = "[101, 102, 105]")
                List<Long> resourceIds) {}
