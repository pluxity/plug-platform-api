package com.pluxity.domains.device.dto;

import com.pluxity.domains.device.entity.Nflux;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.global.response.BaseResponse;

public record NfluxResponse(
        Long id,
        FeatureResponse feature,
        Long categoryId,
        String categoryName,
        Long facilityId,
        String facilityName,
        Long asset,
        String assetName,
        FileResponse icon,
        String iconName,
        String name,
        String code,
        String description,
        BaseResponse baseResponse) {
    public static NfluxResponse from(Nflux device) {
        FileResponse iconResponse =
                device.getIcon() != null
                        ? FileResponse.empty()
                        : null; // 여기 FileService 를 사용하여 실제 아이콘 파일을 가져와야 합니다

        return new NfluxResponse(
                device.getId(),
                device.getFeature() != null ? FeatureResponse.from(device.getFeature()) : null,
                device.getCategory() != null ? device.getCategory().getId() : null,
                device.getCategory() != null ? device.getCategory().getName() : null,
                device.getFacility() != null ? device.getFacility().getId() : null,
                device.getFacility() != null ? device.getFacility().getName() : null,
                device.getFeature() != null && device.getFeature().getAsset() != null
                        ? device.getFeature().getAsset().getId()
                        : null,
                device.getFeature() != null && device.getFeature().getAsset() != null
                        ? device.getFeature().getAsset().getName()
                        : null,
                iconResponse,
                device.getIcon() != null ? device.getIcon().getName() : null,
                device.getName(),
                device.getCode(),
                device.getDescription(),
                BaseResponse.of(device));
    }
}
