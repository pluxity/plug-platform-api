package com.pluxity.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AssetCategoryAllResponse(
        @Schema(description = "최대 depth", example = "3") int maxDepth,
        @Schema(description = "카테고리 목록") List<AssetCategoryResponse> list) {
    public static AssetCategoryAllResponse of(int maxDepth, List<AssetCategoryResponse> list) {
        return new AssetCategoryAllResponse(maxDepth, list);
    }
}
