package com.pluxity.user.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.global.response.BaseResponse;
import com.pluxity.user.entity.Permission;

public record PermissionResponse(
        Long id, String resourceName, String resourceId, @JsonUnwrapped BaseResponse baseResponse) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getResourceName(),
                permission.getResourceId(),
                BaseResponse.of(permission));
    }
}
