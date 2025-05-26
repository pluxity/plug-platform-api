package com.pluxity.device.dto;

import com.pluxity.device.entity.DeviceCategory;
import lombok.Builder;

@Builder
public record DeviceCategoryResponse(
        Long id, String name, Long parentId, Long iconId, Long iconFileId) {
    public static DeviceCategoryResponse from(DeviceCategory deviceCategory) {
        return DeviceCategoryResponse.builder()
                .id(deviceCategory.getId())
                .name(deviceCategory.getName())
                .parentId(deviceCategory.getParent() != null ? deviceCategory.getParent().getId() : null)
                .iconFileId(deviceCategory.getIconFileId() != null ? deviceCategory.getIconFileId() : null)
                .build();
    }
}
