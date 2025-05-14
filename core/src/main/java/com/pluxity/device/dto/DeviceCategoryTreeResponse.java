package com.pluxity.device.dto;

import com.pluxity.device.entity.DeviceCategory;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public record DeviceCategoryTreeResponse(
        Long id, String name, Long iconId, String iconName, List<DeviceCategoryTreeResponse> children) {

    public static DeviceCategoryTreeResponse from(DeviceCategory deviceCategory) {
        return DeviceCategoryTreeResponse.builder()
                .id(deviceCategory.getId())
                .name(deviceCategory.getName())
                .iconId(deviceCategory.getIcon() != null ? deviceCategory.getIcon().getId() : null)
                .iconName(deviceCategory.getIcon() != null ? deviceCategory.getIcon().getName() : null)
                .children(
                        deviceCategory.getChildren().stream()
                                .map(DeviceCategoryTreeResponse::from)
                                .collect(Collectors.toList()))
                .build();
    }
}
