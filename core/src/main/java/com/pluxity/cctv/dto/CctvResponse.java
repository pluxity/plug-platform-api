package com.pluxity.cctv.dto;

import com.pluxity.device.dto.DeviceCategoryResponseWithoutChildren;
import com.pluxity.feature.dto.FeatureResponse;
import lombok.Builder;

@Builder
public record CctvResponse(
        String id,
        String name,
        String url,
        FeatureResponse feature,
        DeviceCategoryResponseWithoutChildren deviceCategory) {}
