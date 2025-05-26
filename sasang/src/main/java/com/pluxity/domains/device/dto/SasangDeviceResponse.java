package com.pluxity.domains.device.dto;

import com.pluxity.domains.device.entity.SasangDevice;
import com.pluxity.feature.dto.FeatureResponse;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record SasangDeviceResponse(
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
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static SasangDeviceResponse from(SasangDevice device) {
        return SasangDeviceResponse.builder()
                .id(device.getId())
                .feature(device.getFeature() != null ? FeatureResponse.from(device.getFeature()) : null)
                .categoryId(device.getCategory() != null ? device.getCategory().getId() : null)
                .categoryName(device.getCategory() != null ? device.getCategory().getName() : null)
                .facilityId(device.getFacility() != null ? device.getFacility().getId() : null)
                .facilityName(device.getFacility() != null ? device.getFacility().getName() : null)
                .asset(
                        device.getFeature() != null && device.getFeature().getAsset() != null
                                ? device.getFeature().getAsset().getId()
                                : null)
                .assetName(
                        device.getFeature() != null && device.getFeature().getAsset() != null
                                ? device.getFeature().getAsset().getName()
                                : null)
                .iconId(device.getIcon() != null ? device.getIcon().getId() : null)
                .iconName(device.getIcon() != null ? device.getIcon().getName() : null)
                .name(device.getName())
                .code(device.getCode())
                .description(device.getDescription())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
}
