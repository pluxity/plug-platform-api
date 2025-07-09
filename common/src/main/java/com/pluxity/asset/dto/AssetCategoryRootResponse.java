package com.pluxity.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AssetCategoryRootResponse(
        @Schema(description = "최대 depth", example = "3") int maxDepth,
        @Schema(description = "카테고리 목록") List<AssetCategoryResponse> list) {
    public static AssetCategoryRootResponse of(int maxDepth, List<AssetCategoryResponse> list) {
        return new AssetCategoryRootResponse(maxDepth, list);
    }
}
