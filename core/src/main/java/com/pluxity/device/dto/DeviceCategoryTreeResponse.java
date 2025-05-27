package com.pluxity.device.dto;

import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.file.dto.FileResponse;
import java.util.List;
import java.util.stream.Collectors;

public record DeviceCategoryTreeResponse(
        Long id, String name, FileResponse iconFile, List<DeviceCategoryTreeResponse> children) {

    public static DeviceCategoryTreeResponse from(DeviceCategory deviceCategory) {
        return new DeviceCategoryTreeResponse(
                deviceCategory.getId(),
                deviceCategory.getName(),
                null,
                deviceCategory.getChildren().stream()
                        .map(DeviceCategoryTreeResponse::from)
                        .collect(Collectors.toList()));
    }

    public static DeviceCategoryTreeResponse from(
            DeviceCategory deviceCategory, FileResponse iconFile) {
        return new DeviceCategoryTreeResponse(
                deviceCategory.getId(),
                deviceCategory.getName(),
                iconFile,
                deviceCategory.getChildren().stream()
                        .map(child -> DeviceCategoryTreeResponse.from(child, null))
                        .collect(Collectors.toList()));
    }
}
