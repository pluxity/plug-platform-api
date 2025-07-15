package com.pluxity.facility.category.dto;

import com.pluxity.facility.category.FacilityCategory;
import com.pluxity.file.dto.FileResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record FacilityCategoryResponse(
        @Schema(description = "카테고리 ID", example = "1") Long id,
        @Schema(description = "카테고리 이름", example = "그래픽 에셋") String name,
        @Schema(description = "부모 카테고리 ID", example = "2") Long parentId,
        @Schema(
                        description = "자식 카테고리 목록",
                        example =
                                "[{\"id\":2,\"name\":\"서브 카테고리\",\"parentId\":1,\"children\":[],\"createdAt\":\"string\",\"updatedAt\":\"string\",\"depth\":2}]")
                List<FacilityCategoryResponse> children,
        @Schema(description = "아이콘 파일 정보") FileResponse thumbnail,
        @Schema(description = "생성일시") LocalDateTime createdAt,
        @Schema(description = "수정일시") LocalDateTime updatedAt,
        @Schema(description = "depth") int depth) {
    public static FacilityCategoryResponse from(FacilityCategory category) {
        return new FacilityCategoryResponse(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getChildren().stream()
                        .map(FacilityCategoryResponse::from)
                        .collect(Collectors.toList()),
                null,
                category.getCreatedAt(),
                category.getUpdatedAt(),
                category.getDepth());
    }

    public static FacilityCategoryResponse from(FacilityCategory category, FileResponse iconFile) {
        return new FacilityCategoryResponse(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getChildren().stream()
                        .map(FacilityCategoryResponse::from)
                        .collect(Collectors.toList()),
                iconFile,
                category.getCreatedAt(),
                category.getUpdatedAt(),
                category.getDepth());
    }
}
