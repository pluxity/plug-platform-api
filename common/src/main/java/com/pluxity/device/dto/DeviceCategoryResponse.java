package com.pluxity.device.dto;

import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.file.dto.FileResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.stream.Collectors;

public record DeviceCategoryResponse(
        Long id,
        String name,
        Long parentId,
        @Schema(
                        description = "자식 카테고리 목록",
                        example =
                                "[{\"id\":2,\"name\":\"서브 카테고리\",\"code\":\"SUB\",\"parentId\":1,\"children\":[],\"thumbnail\":null,\"assetIds\":[0],\"createdAt\":\"string\",\"updatedAt\":\"string\",\"depth\":2}]")
                List<DeviceCategoryResponse> children,
        FileResponse thumbnailFile,
        @Schema(description = "depth", example = "1") int depth) {

    public static DeviceCategoryResponse from(DeviceCategory deviceCategory) {
        return new DeviceCategoryResponse(
                deviceCategory.getId(),
                deviceCategory.getName(),
                deviceCategory.getParent() != null ? deviceCategory.getParent().getId() : null,
                deviceCategory.getChildren().stream()
                        .map(DeviceCategoryResponse::from)
                        .collect(Collectors.toList()),
                null,
                deviceCategory.getDepth());
    }

    public static DeviceCategoryResponse from(DeviceCategory deviceCategory, FileResponse iconFile) {
        return new DeviceCategoryResponse(
                deviceCategory.getId(),
                deviceCategory.getName(),
                deviceCategory.getParent() != null ? deviceCategory.getParent().getId() : null,
                deviceCategory.getChildren().stream()
                        .map(DeviceCategoryResponse::from)
                        .collect(Collectors.toList()),
                iconFile != null ? iconFile : FileResponse.empty(),
                deviceCategory.getDepth());
    }

    public static DeviceCategoryResponse fromWithoutChildren(
            DeviceCategory deviceCategory, FileResponse iconFile) {
        return new DeviceCategoryResponse(
                deviceCategory.getId(),
                deviceCategory.getName(),
                deviceCategory.getParent() != null ? deviceCategory.getParent().getId() : null,
                List.of(), // 자식 목록을 빈 리스트로 설정
                iconFile,
                deviceCategory.getDepth());
    }
}
