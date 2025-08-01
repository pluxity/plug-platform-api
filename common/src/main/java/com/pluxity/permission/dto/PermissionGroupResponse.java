package com.pluxity.permission.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.global.response.BaseResponse;
import com.pluxity.permission.Permission;
import com.pluxity.permission.PermissionGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record PermissionGroupResponse(
        @Schema(description = "권한 그룹 ID") Long id,
        @Schema(description = "권한 그룹 이름") String name,
        @Schema(description = "권한 그룹 설명") String description,
        @Schema(description = "포함된 권한 목록 ") List<PermissionResponse> permissions,
        @JsonUnwrapped BaseResponse baseResponse) {

    public static PermissionGroupResponse from(PermissionGroup permissionGroup) {
        Map<String, List<Permission>> groupedPermissions =
                permissionGroup.getPermissions().stream()
                        .collect(Collectors.groupingBy(Permission::getResourceName));

        List<PermissionResponse> permissionDtos =
                groupedPermissions.entrySet().stream()
                        .map(
                                entry -> {
                                    String resourceType = entry.getKey();
                                    List<String> resourceIds =
                                            entry.getValue().stream()
                                                    .map(Permission::getResourceId)
                                                    .collect(Collectors.toList());
                                    return new PermissionResponse(resourceType, resourceIds);
                                })
                        .toList();

        return new PermissionGroupResponse(
                permissionGroup.getId(),
                permissionGroup.getName(),
                permissionGroup.getDescription(),
                permissionDtos,
                BaseResponse.of(permissionGroup));
    }
}
