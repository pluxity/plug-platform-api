package com.pluxity.device.dto;

import com.pluxity.device.entity.DefaultDevice;
import com.pluxity.feature.dto.FeatureResponse;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DeviceResponse(
        Long id,
        FeatureResponse feature,
        Long categoryId,
        String categoryName,
        Long stationId,
        String stationName,
        Long asset2dId,
        Long asset3dId,
        String assetName,
        String name,
        String code,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DeviceResponse from(DefaultDevice device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .feature(device.getFeature() != null ? FeatureResponse.from(device.getFeature()) : null)
                .categoryId(device.getCategory() != null ? device.getCategory().getId() : null)
                .categoryName(device.getCategory() != null ? device.getCategory().getName() : null)
                .stationId(device.getStation() != null ? device.getStation().getId() : null)
                .stationName(device.getStation() != null ? device.getStation().getName() : null)
                .asset2dId(device.getAsset2d() != null ? device.getAsset2d().getId() : null)
                .asset3dId(device.getAsset3d() != null ? device.getAsset3d().getId() : null)
                .assetName(device.getAsset2d() != null ? device.getAsset2d().getName() : null)
                .name(device.getName())
                .code(device.getCode())
                .description(device.getDescription())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
} 