package com.pluxity.domains.device_category_acl.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record GrantPermissionRequest(
        @NotBlank(message = "대상 유형은 필수 항목입니다") String targetType,
        @NotNull(message = "대상 ID는 필수 항목입니다") Long targetId,
        @NotBlank(message = "사용자명은 필수 항목입니다") String principalName,
        boolean isRole,
        @NotNull(message = "권한 목록은 필수 항목입니다") List<String> permissions) {}
