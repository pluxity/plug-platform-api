package com.pluxity.domains.device.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.domains.device.entity.Nflux;
import com.pluxity.feature.dto.FeatureResponseWithoutAsset;
import com.pluxity.global.response.BaseResponse;

public record NfluxDetailResponse(
        Long id,
        FeatureResponseWithoutAsset feature,
        Long categoryId,
        String categoryName,
        Long asset,
        String assetName,
        String name,
        String code,
        String description,
        @JsonUnwrapped BaseResponse baseResponse) {
    public static NfluxDetailResponse from(Nflux device) {
        return new NfluxDetailResponse(
                device.getId(),
                device.getFeature() != null ? FeatureResponseWithoutAsset.from(device.getFeature()) : null,
                device.getCategory() != null ? device.getCategory().getId() : null,
                device.getCategory() != null ? device.getCategory().getName() : null,
                device.getFeature() != null && device.getFeature().getAsset() != null
                        ? device.getFeature().getAsset().getId()
                        : null,
                device.getFeature() != null && device.getFeature().getAsset() != null
                        ? device.getFeature().getAsset().getName()
                        : null,
                device.getName(),
                device.getDeviceCode(),
                device.getDescription(),
                BaseResponse.of(device));
    }
}
