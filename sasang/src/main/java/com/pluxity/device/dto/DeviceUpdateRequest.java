package com.pluxity.device.dto;

import com.pluxity.feature.dto.FeatureUpdateRequest;

public record DeviceUpdateRequest(
        FeatureUpdateRequest feature,
        Long deviceCategoryId,
        Long stationId,
        Long asset2dId,
        Long asset3dId,
        String name,
        String code,
        String description) {}
