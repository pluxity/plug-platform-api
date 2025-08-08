package com.pluxity.device.dto;

import com.pluxity.feature.dto.FeatureResponse;
import lombok.Builder;

@Builder
public record GsDeviceResponse(
        String id,
        String name,
        FeatureResponse feature,
        DeviceCategoryResponseWithoutChildren deviceCategory) {}
