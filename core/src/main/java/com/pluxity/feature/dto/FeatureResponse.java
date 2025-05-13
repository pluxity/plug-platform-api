package com.pluxity.feature.dto;

import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.Spatial;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record FeatureResponse(
        Long id,
        Spatial position,
        Spatial rotation,
        Spatial scale,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static FeatureResponse from(Feature feature) {
        return FeatureResponse.builder()
                .id(feature.getId())
                .position(feature.getPosition())
                .rotation(feature.getRotation())
                .scale(feature.getScale())
                .createdAt(feature.getCreatedAt())
                .updatedAt(feature.getUpdatedAt())
                .build();
    }
}
