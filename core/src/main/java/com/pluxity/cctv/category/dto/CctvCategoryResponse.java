package com.pluxity.cctv.category.dto;

import com.pluxity.cctv.category.CctvCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CctvCategoryResponse(
        @Schema(description = "카테고리 ID", example = "1") Long id,
        @Schema(description = "카테고리 이름", example = "카테고리") String name,
        @Schema(description = "부모 카테고리 ID", example = "2") Long parentId,
        @Schema(
                        description = "자식 카테고리 목록",
                        example =
                                "[{\"id\":2,\"name\":\"서브 카테고리\",\"parentId\":1,\"children\":[],\"createdAt\":\"string\",\"updatedAt\":\"string\",\"depth\":2}]")
                List<CctvCategoryResponse> children,
        @Schema(description = "생성일시") LocalDateTime createdAt,
        @Schema(description = "수정일시") LocalDateTime updatedAt,
        @Schema(description = "depth", example = "1") int depth) {
    public static CctvCategoryResponse from(CctvCategory category) {
        return new CctvCategoryResponse(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null,
                new ArrayList<>(),
                category.getCreatedAt(),
                category.getUpdatedAt(),
                category.getDepth());
    }
}
