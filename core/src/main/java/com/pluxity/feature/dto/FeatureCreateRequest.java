package com.pluxity.feature.dto;

import com.pluxity.feature.entity.Spatial;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record FeatureCreateRequest(
        @NotBlank String id, // UUID
        Spatial position,
        Spatial rotation,
        Spatial scale,
        Long assetId) {}
