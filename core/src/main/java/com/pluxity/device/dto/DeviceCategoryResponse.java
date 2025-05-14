package com.pluxity.device.dto;

import com.pluxity.device.entity.DeviceCategory;
import lombok.Builder;

@Builder
public record DeviceCategoryResponse(
        Long id, String name, Long parentId, Long iconId, String iconName) {
    public static DeviceCategoryResponse from(DeviceCategory deviceCategory) {
        return DeviceCategoryResponse.builder()
                .id(deviceCategory.getId())
                .name(deviceCategory.getName())
                .parentId(deviceCategory.getParent() != null ? deviceCategory.getParent().getId() : null)
                .iconId(deviceCategory.getIcon() != null ? deviceCategory.getIcon().getId() : null)
                .iconName(deviceCategory.getIcon() != null ? deviceCategory.getIcon().getName() : null)
                .build();
    }
}
