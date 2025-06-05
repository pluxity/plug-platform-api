package com.pluxity.feature.dto;

import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.Spatial;
import com.pluxity.file.dto.FileResponse;

public record FeatureResponseWithoutAsset(
        String id,
        Spatial position,
        Spatial rotation,
        Spatial scale,
        Long assetId,
        String floorId,
        String deviceId) {

    public static FeatureResponseWithoutAsset from(
            Feature feature,
            FileResponse assetFile,
            FileResponse assetThumbnail,
            FileResponse facilityDrawing,
            FileResponse facilityThumbnail) {

        return new FeatureResponseWithoutAsset(
                feature.getId(),
                feature.getPosition(),
                feature.getRotation(),
                feature.getScale(),
                feature.getAsset() != null ? feature.getAsset().getId() : null,
                feature.getFloorId(),
                feature.getDevice() != null ? feature.getDevice().getId() : null);
    }

    public static FeatureResponseWithoutAsset from(Feature feature) {

        return new FeatureResponseWithoutAsset(
                feature.getId(),
                feature.getPosition(),
                feature.getRotation(),
                feature.getScale(),
                feature.getAsset() != null ? feature.getAsset().getId() : null,
                feature.getFloorId(),
                feature.getDevice() != null ? feature.getDevice().getId() : null);
    }
}
