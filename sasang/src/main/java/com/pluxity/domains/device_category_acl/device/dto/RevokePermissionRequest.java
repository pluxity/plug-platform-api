package com.pluxity.domains.device_category_acl.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

// 특정 객체에 대한 권한 회수 요청
public record RevokePermissionRequest(
        @NotBlank(message = "대상 유형은 필수 항목입니다") String targetType,
        @NotNull(message = "대상 ID는 필수 항목입니다") Long targetId,
        @NotBlank(message = "사용자명은 필수 항목입니다") String principalName,
        boolean isRole,
        List<String> permissions, // 회수할 권한 목록, 비어있거나 null이면 해당 principal의 모든 권한 회수
        boolean removeAll) {}
