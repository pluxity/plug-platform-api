package com.pluxity.feature.dto;

import com.pluxity.feature.entity.Spatial;
import lombok.Builder;

@Builder
public record FeatureCreateRequest(
    Spatial position,
    Spatial rotation,
    Spatial scale
) {
}
