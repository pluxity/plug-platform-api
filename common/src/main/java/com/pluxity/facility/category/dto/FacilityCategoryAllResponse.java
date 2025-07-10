package com.pluxity.facility.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record FacilityCategoryAllResponse(
        @Schema(description = "최대 depth", example = "3") int maxDepth,
        @Schema(description = "카테고리 목록") List<FacilityCategoryResponse> list) {
    public static FacilityCategoryAllResponse of(int maxDepth, List<FacilityCategoryResponse> list) {
        return new FacilityCategoryAllResponse(maxDepth, list);
    }
}
