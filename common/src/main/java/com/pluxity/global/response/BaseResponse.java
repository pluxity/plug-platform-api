package com.pluxity.global.response;

import com.pluxity.global.entity.BaseEntity;

public record BaseResponse(String createdAt, String createdBy, String updatedAt, String updatedBy) {

    public static BaseResponse of(BaseEntity entity) {
        return new BaseResponse(
                entity.getCreatedAt().toString(),
                entity.getCreatedBy(),
                entity.getUpdatedAt().toString(),
                entity.getUpdatedBy());
    }
}
