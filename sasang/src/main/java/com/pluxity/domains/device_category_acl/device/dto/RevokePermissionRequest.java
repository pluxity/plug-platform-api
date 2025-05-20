package com.pluxity.domains.device_category_acl.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 특정 객체에 대한 권한 회수 요청
 *
 * @deprecated 새로운 {@link PermissionRequestDto} 클래스를 사용하세요. PermissionRequestDto는 권한 부여와 회수를 통합적으로
 *     처리하며, 하나의 요청으로 여러 대상에 대한 권한 관리가 가능합니다.
 */
@Deprecated
public record RevokePermissionRequest(
        @NotBlank(message = "대상 유형은 필수 항목입니다") String targetType,
        @NotNull(message = "대상 ID는 필수 항목입니다") Long targetId,
        @NotBlank(message = "사용자명은 필수 항목입니다") String principalName,
        boolean isRole,
        List<String> permissions, // 회수할 권한 목록, 비어있거나 null이면 해당 principal의 모든 권한 회수
        boolean removeAll) {}
