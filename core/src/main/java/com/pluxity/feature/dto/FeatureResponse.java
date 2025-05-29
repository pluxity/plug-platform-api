package com.pluxity.feature.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.asset.dto.AssetResponse;
import com.pluxity.facility.facility.dto.FacilityResponse;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.Spatial;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.global.response.BaseResponse;

public record FeatureResponse(
        String id,
        Spatial position,
        Spatial rotation,
        Spatial scale,
        AssetResponse asset,
        FacilityResponse facilityId,
        Long floorId,
        String deviceCode,
        @JsonUnwrapped BaseResponse baseResponse) {

    public static FeatureResponse from(
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

        return new FeatureResponse(
                feature.getId(),
                feature.getPosition(),
                feature.getRotation(),
                feature.getScale(),
                feature.getAsset() != null
                        ? AssetResponse.from(feature.getAsset(), assetFile, assetThumbnail)
                        : null,
                feature.getFacility() != null
                        ? FacilityResponse.from(feature.getFacility(), facilityDrawing, facilityThumbnail)
                        : null,
                feature.getFloorId(),
                deviceCode,
                BaseResponse.of(feature));
    }

    public static FeatureResponse from(Feature feature) {
        // Device에서 code 정보 가져오기
        String deviceCode = null;
        if (feature.getDevice() != null) {
            deviceCode = feature.getDevice().getDeviceCode();
        }

        return new FeatureResponse(
                feature.getId(),
                feature.getPosition(),
                feature.getRotation(),
                feature.getScale(),
                null,
                null,
                feature.getFloorId(),
                deviceCode,
                BaseResponse.of(feature));
    }
}
