package com.pluxity.feature.dto;

import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.Spatial;

public record FeatureResponse(
        String id, Spatial position, Spatial rotation, Spatial scale, Long assetId, String floorId) {

    public static FeatureResponse from(Feature feature) {
        return new FeatureResponse(
                feature.getId(),
                feature.getPosition(),
                feature.getRotation(),
                feature.getScale(),
                feature.getAssetId(),
                feature.getFloorId());
    }
}
