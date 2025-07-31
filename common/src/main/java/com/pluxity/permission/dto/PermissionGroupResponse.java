package com.pluxity.permission.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.global.response.BaseResponse;
import com.pluxity.permission.PermissionGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PermissionGroupResponse(
        @Schema(description = "권한 그룹 ID") Long id,
        @Schema(description = "권한 그룹 이름") String name,
        @Schema(description = "포함된 권한 목록") List<PermissionResponse> permissions,
        @JsonUnwrapped BaseResponse baseResponse) {
    public static PermissionGroupResponse from(PermissionGroup permissionGroup) {
        List<PermissionResponse> permissionResponses =
                permissionGroup.getPermissions().stream().map(PermissionResponse::from).toList();
        return new PermissionGroupResponse(
                permissionGroup.getId(),
                permissionGroup.getName(),
                permissionResponses,
                BaseResponse.of(permissionGroup));
    }
}
