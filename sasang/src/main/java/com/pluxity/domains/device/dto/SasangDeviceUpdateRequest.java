package com.pluxity.domains.device.dto;

import com.pluxity.feature.dto.FeatureUpdateRequest;

public record SasangDeviceUpdateRequest(
        FeatureUpdateRequest feature,
        Long deviceCategoryId,
        Long stationId,
        Long asset,
        Long iconId,
        String name,
        String code,
        String description) {}
