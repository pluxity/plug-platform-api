package com.pluxity.cctv.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CctvCategoryAllResponse(
        @Schema(description = "최대 depth", example = "3") int maxDepth,
        @Schema(description = "카테고리 목록") List<CctvCategoryResponse> list) {
    public static CctvCategoryAllResponse of(int maxDepth, List<CctvCategoryResponse> list) {
        return new CctvCategoryAllResponse(maxDepth, list);
    }
}
