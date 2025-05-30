package com.pluxity.feature.dto;

import com.pluxity.feature.entity.Spatial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeatureCreateRequest(
        @NotBlank String id, // UUID
        Spatial position,
        Spatial rotation,
        Spatial scale,
        @NotNull Long assetId,
        @NotNull Long facilityId,
        @NotNull String floorId) {}
