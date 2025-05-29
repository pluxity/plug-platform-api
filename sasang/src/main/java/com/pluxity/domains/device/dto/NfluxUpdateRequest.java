package com.pluxity.domains.device.dto;

import com.pluxity.feature.dto.FeatureUpdateRequest;

public record NfluxUpdateRequest(
        FeatureUpdateRequest feature,
        Long deviceCategoryId,
        Long asset,
        String name,
        String code,
        String description) {}
