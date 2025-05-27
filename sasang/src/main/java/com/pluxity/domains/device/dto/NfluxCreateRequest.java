package com.pluxity.domains.device.dto;

import com.pluxity.feature.dto.FeatureCreateRequest;

public record NfluxCreateRequest(
        FeatureCreateRequest feature,
        Long deviceCategoryId,
        Long stationId,
        Long asset,
        Long iconId,
        String name,
        String code,
        String description) {}
