package com.pluxity.feature.dto;

import com.pluxity.asset.dto.AssetResponse;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.Spatial;
import com.pluxity.file.dto.FileResponse;

public record FeatureResponse(
        String id,
        Spatial position,
        Spatial rotation,
        Spatial scale,
        AssetResponse asset,
        String floorId,
        String deviceId) {

    public static FeatureResponse from(
            Feature feature,
            FileResponse assetFile,
            FileResponse assetThumbnail,
            FileResponse facilityDrawing,
            FileResponse facilityThumbnail) {
        return new FeatureResponse(
                feature.getId(),
                feature.getPosition(),
                feature.getRotation(),
                feature.getScale(),
                feature.getAsset() != null
                        ? AssetResponse.from(feature.getAsset(), assetFile, assetThumbnail)
                        : null,
                feature.getFloorId(),
                feature.getDevice() != null ? feature.getDevice().getId() : null);
    }

    public static FeatureResponse from(Feature feature) {
        return new FeatureResponse(
                feature.getId(),
                feature.getPosition(),
                feature.getRotation(),
                feature.getScale(),
                feature.getAsset() != null ? AssetResponse.from(feature.getAsset()) : null,
                feature.getFloorId(),
                feature.getDevice() != null ? feature.getDevice().getId() : null);
    }
}
