package com.pluxity.device.dto;

import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.file.dto.FileResponse;
import io.swagger.v3.oas.annotations.media.Schema;

public record DeviceCategoryResponseWithoutChildren(
        Long id,
        String name,
        Long parentId,
        FileResponse thumbnailFile,
        @Schema(description = "depth", example = "1") int depth) {

    public static DeviceCategoryResponseWithoutChildren from(DeviceCategory deviceCategory) {
        return new DeviceCategoryResponseWithoutChildren(
                deviceCategory.getId(),
                deviceCategory.getName(),
                deviceCategory.getParent() != null ? deviceCategory.getParent().getId() : null,
                null,
                deviceCategory.getDepth());
    }

    public static DeviceCategoryResponseWithoutChildren from(
            DeviceCategory deviceCategory, FileResponse iconFile) {
        return new DeviceCategoryResponseWithoutChildren(
                deviceCategory.getId(),
                deviceCategory.getName(),
                deviceCategory.getParent() != null ? deviceCategory.getParent().getId() : null,
                iconFile != null ? iconFile : FileResponse.empty(),
                deviceCategory.getDepth());
    }
}
