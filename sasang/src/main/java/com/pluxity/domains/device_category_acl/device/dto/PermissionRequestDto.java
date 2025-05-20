package com.pluxity.domains.device_category_acl.device.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 권한 관리 요청 DTO 하나의 대상 유형(targetType)과 주체(principalName)에 대해 여러 대상 ID(targetId)에 권한 부여(GRANT) 또는
 * 회수(REVOKE) 작업을 수행합니다.
 */
public record PermissionRequestDto(
        @NotBlank(message = "대상 유형은 필수 항목입니다") String targetType,
        @NotBlank(message = "역할 이름은 필수 항목입니다") String principalName,
        @NotEmpty(message = "권한 대상 목록은 필수 항목입니다") List<PermissionTarget> targets) {

    @JsonIgnore
    public boolean isRole() {
        return true;
    }

    @JsonIgnore
    public List<String> getPermissions() {
        return List.of("READ", "WRITE", "CREATE", "DELETE");
    }

    /**
     * 권한 부여/회수할 대상
     *
     * @param targetId 대상 객체의 ID
     * @param operation 수행할 작업 (GRANT: 권한 부여, REVOKE: 권한 회수)
     */
    public record PermissionTarget(
            @NotNull(message = "대상 ID는 필수 항목입니다") Long targetId,
            @NotNull(message = "권한 유형은 필수 항목입니다") PermissionOperation operation) {}

    public enum PermissionOperation {
        GRANT,
        REVOKE
    }
}
