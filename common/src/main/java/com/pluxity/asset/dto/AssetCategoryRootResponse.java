package com.pluxity.asset.dto;

import java.util.List;

public record AssetCategoryRootResponse(
        int maxDepth,
        List<AssetCategoryResponse> list
) {
    public static AssetCategoryRootResponse of(int maxDepth, List<AssetCategoryResponse> list) {
        return new AssetCategoryRootResponse(maxDepth, list);
    }
}
