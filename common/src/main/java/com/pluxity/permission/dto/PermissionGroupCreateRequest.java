package com.pluxity.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PermissionGroupCreateRequest(
        @NotNull @Schema(description = "권한 집합 이름") String name,
        @Schema(description = "권한에 대한 설명") String description,
        @NotNull @Schema(description = "권한의 상세 목록") List<PermissionRequest> permissions) {}
