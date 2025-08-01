package com.pluxity.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record DeviceCategoryAllResponse(
        @Schema(description = "최대 depth", example = "3") int maxDepth,
        @Schema(description = "카테고리 목록") List<DeviceCategoryResponse> list) {
    public static DeviceCategoryAllResponse of(int maxDepth, List<DeviceCategoryResponse> list) {
        return new DeviceCategoryAllResponse(maxDepth, list);
    }
}
