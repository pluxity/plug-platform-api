package com.pluxity.domains.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record NfluxCategoryAllResponse(
        @Schema(description = "최대 depth", example = "3") int maxDepth,
        @Schema(description = "카테고리 목록") List<NfluxCategoryResponse> list) {
    public static NfluxCategoryAllResponse of(int maxDepth, List<NfluxCategoryResponse> list) {
        return new NfluxCategoryAllResponse(maxDepth, list);
    }
}
