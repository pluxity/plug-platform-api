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
        Long assetId,
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
                .assetId(device.getAsset() != null ? device.getAsset().getId() : null)
                .assetName(device.getAsset() != null ? device.getAsset().getName() : null)
                .name(device.getName())
                .code(device.getCode())
                .description(device.getDescription())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
} 