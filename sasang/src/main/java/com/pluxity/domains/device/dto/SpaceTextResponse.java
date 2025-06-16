package com.pluxity.domains.device.dto;

import com.pluxity.domains.device.entity.SpaceText;
import com.pluxity.feature.dto.FeatureResponseWithoutAsset;
import java.time.LocalDateTime;

public record SpaceTextResponse(
        String id,
        String textContent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        FeatureResponseWithoutAsset feature) {
    public static SpaceTextResponse from(SpaceText spaceText) {
        return new SpaceTextResponse(
                spaceText.getId(),
                spaceText.getTextContent(),
                spaceText.getCreatedAt(),
                spaceText.getUpdatedAt(),
                spaceText.getFeature() != null
                        ? FeatureResponseWithoutAsset.from(spaceText.getFeature())
                        : null);
    }
}
