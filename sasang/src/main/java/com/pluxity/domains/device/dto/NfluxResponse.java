package com.pluxity.domains.device.dto;

import com.pluxity.domains.device.entity.Nflux;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.global.response.BaseResponse;

public record NfluxResponse(
        Long id,
        FeatureResponse feature,
        Long categoryId,
        String categoryName,
        Long asset,
        String assetName,
        String name,
        String code,
        String description,
        BaseResponse baseResponse) {
    public static NfluxResponse from(Nflux device) {
        return new NfluxResponse(
                device.getId(),
                device.getFeature() != null ? FeatureResponse.from(device.getFeature()) : null,
                device.getCategory() != null ? device.getCategory().getId() : null,
                device.getCategory() != null ? device.getCategory().getName() : null,
                device.getFeature() != null && device.getFeature().getAsset() != null
                        ? device.getFeature().getAsset().getId()
                        : null,
                device.getFeature() != null && device.getFeature().getAsset() != null
                        ? device.getFeature().getAsset().getName()
                        : null,
                device.getName(),
                device.getCode(),
                device.getDescription(),
                BaseResponse.of(device));
    }
}
