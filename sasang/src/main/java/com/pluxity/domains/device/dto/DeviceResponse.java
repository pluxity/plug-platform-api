package com.pluxity.domains.device.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.domains.device.entity.DefaultDevice;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.global.response.BaseResponse;
import lombok.Builder;

@Builder
public record DeviceResponse(
        Long id,
        FeatureResponse feature,
        Long categoryId,
        String categoryName,
        Long facilityId,
        String facilityName,
        Long asset,
        String assetName,
        Long iconId,
        String iconName,
        String name,
        String code,
        String description,
        @JsonUnwrapped BaseResponse baseResponse) {
    public static DeviceResponse from(DefaultDevice device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .feature(device.getFeature() != null ? FeatureResponse.from(device.getFeature()) : null)
                .categoryId(device.getCategory() != null ? device.getCategory().getId() : null)
                .categoryName(device.getCategory() != null ? device.getCategory().getName() : null)
                .facilityId(device.getFacility() != null ? device.getFacility().getId() : null)
                .facilityName(device.getFacility() != null ? device.getFacility().getName() : null)
                .asset(device.getAsset() != null ? device.getAsset().getId() : null)
                .assetName(device.getAsset() != null ? device.getAsset().getName() : null)
                .iconId(device.getIcon() != null ? device.getIcon().getId() : null)
                .iconName(device.getIcon() != null ? device.getIcon().getName() : null)
                .name(device.getName())
                .code(device.getCode())
                .description(device.getDescription())
                .baseResponse(BaseResponse.of(device))
                .build();
    }
}
