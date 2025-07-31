package com.pluxity.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PermissionRequest(
        @NotNull @Schema(description = "자원 유형") String resourceType,
        @NotNull @Schema(description = "자원 아이디") List<String> resourceIds) {}
