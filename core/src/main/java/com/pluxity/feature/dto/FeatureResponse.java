package com.pluxity.feature.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.asset.dto.AssetResponse;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.Spatial;
import com.pluxity.global.response.BaseResponse;
import lombok.Builder;

@Builder
public record FeatureResponse(
        String id,
        Spatial position,
        Spatial rotation,
        Spatial scale,
        AssetResponse asset,
        @JsonUnwrapped BaseResponse baseResponse) {

    public static FeatureResponse from(Feature feature) {
        return FeatureResponse.builder()
                .id(feature.getId())
                .position(feature.getPosition())
                .rotation(feature.getRotation())
                .scale(feature.getScale())
                .asset(feature.getAsset() != null ? AssetResponse.from(feature.getAsset()) : null)
                .baseResponse(com.pluxity.global.response.BaseResponse.of(feature))
                .build();
    }
}
