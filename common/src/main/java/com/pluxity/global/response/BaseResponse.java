package com.pluxity.global.response;

import com.pluxity.global.entity.BaseEntity;

public record BaseResponse(String createdAt, String createdBy, String updatedAt, String updatedBy) {

    public static BaseResponse of(BaseEntity entity) {
        return new BaseResponse(
                entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null,
                entity.getCreatedBy() != null ? entity.getCreatedBy() : null,
                entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null,
                entity.getUpdatedBy() != null ? entity.getUpdatedBy() : null);
    }
}
