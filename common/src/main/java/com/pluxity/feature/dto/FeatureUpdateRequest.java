package com.pluxity.feature.dto;

import com.pluxity.feature.entity.Spatial;

public record FeatureUpdateRequest(Spatial position, Spatial rotation, Spatial scale) {}
