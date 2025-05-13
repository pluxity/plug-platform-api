package com.pluxity.device.dto;

import com.pluxity.feature.dto.FeatureUpdateRequest;

public record DeviceUpdateRequest(
        FeatureUpdateRequest feature,
        Long deviceCategoryId,
        Long stationId,
        Long asset,
        String name,
        String code,
        String description) {}
