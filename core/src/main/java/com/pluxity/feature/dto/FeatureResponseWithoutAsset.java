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
        String deviceCode) {

    public static FeatureResponseWithoutAsset from(
            Feature feature,
            FileResponse assetFile,
            FileResponse assetThumbnail,
            FileResponse facilityDrawing,
            FileResponse facilityThumbnail) {

        // Device에서 code 정보 가져오기
        String deviceCode = null;
        if (feature.getDevice() != null) {
            deviceCode = feature.getDevice().getDeviceCode();
        }

        return new FeatureResponseWithoutAsset(
                feature.getId(),
                feature.getPosition(),
                feature.getRotation(),
                feature.getScale(),
                feature.getAsset() != null ? feature.getAsset().getId() : null,
                feature.getFloorId(),
                deviceCode);
    }

    public static FeatureResponseWithoutAsset from(Feature feature) {
        // Device에서 code 정보 가져오기
        String deviceCode = null;
        if (feature.getDevice() != null) {
            deviceCode = feature.getDevice().getDeviceCode();
        }

        return new FeatureResponseWithoutAsset(
                feature.getId(),
                feature.getPosition(),
                feature.getRotation(),
                feature.getScale(),
                feature.getAsset() != null ? feature.getAsset().getId() : null,
                feature.getFloorId(),
                deviceCode);
    }
}
