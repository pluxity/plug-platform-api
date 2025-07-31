package com.pluxity.device.dto;

import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.file.dto.FileResponse;

public record DeviceCategoryResponse(
        Long id, String name, Long parentId, FileResponse thumbnailFile) {
    public static DeviceCategoryResponse from(DeviceCategory deviceCategory, FileResponse iconFile) {
        return new DeviceCategoryResponse(
                deviceCategory.getId(),
                deviceCategory.getName(),
                deviceCategory.getParent() != null ? deviceCategory.getParent().getId() : null,
                iconFile != null ? iconFile : FileResponse.empty());
    }

    public static DeviceCategoryResponse from(DeviceCategory deviceCategory) {
        return from(deviceCategory, null);
    }
}
