package com.pluxity.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PermissionCreateRequest(
        @NotNull @Schema(description = "권한 집합 이름") String permissionSetName,
        @NotNull
                @Schema(
                        description = "권한을 관리할 리소스의 타입",
                        example = "시설",
                        allowableValues = {"시설", "장비 분류"})
                String resourceName,
        @NotBlank @Schema(description = "권한을 부여/관리할 리소스의 ID", example = "[101, 102]")
                List<String> resourceIds) {}
