package com.pluxity.device.dto;

import com.pluxity.feature.dto.FeatureCreateRequest;

public record DeviceCreateRequest(
        FeatureCreateRequest feature,
        Long deviceCategoryId,
        Long stationId,
        Long assetId,
        String name,
        String code,
        String description
) {
}
