package com.pluxity.asset.dto;

import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.entity.AssetCategory;
import com.pluxity.file.dto.FileResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record AssetCategoryResponse(
        @Schema(description = "카테고리 ID", example = "1") Long id,
        @Schema(description = "카테고리 이름", example = "그래픽 에셋") String name,
        @Schema(description = "카테고리 코드", example = "GRAPHIC_ASSET") String code,
        @Schema(description = "부모 카테고리 ID", example = "2") Long parentId,
        @Schema(description = "자식 카테고리 목록") List<AssetCategoryResponse> children,
        @Schema(description = "아이콘 파일 정보") FileResponse iconFile,
        @Schema(description = "소속 에셋 ID 목록") List<Long> assetIds,
        @Schema(description = "생성일시") LocalDateTime createdAt,
        @Schema(description = "수정일시") LocalDateTime updatedAt) {
    public static AssetCategoryResponse from(AssetCategory category) {
        return new AssetCategoryResponse(
                category.getId(),
                category.getName(),
                category.getCode(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getChildren().stream()
                        .map(AssetCategoryResponse::from)
                        .collect(Collectors.toList()),
                null, // 서비스에서 FileService를 통해 설정할 예정
                category.getAssets().stream().map(Asset::getId).collect(Collectors.toList()),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }

    public static AssetCategoryResponse from(AssetCategory category, FileResponse iconFile) {
        return new AssetCategoryResponse(
                category.getId(),
                category.getName(),
                category.getCode(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getChildren().stream()
                        .map(child -> AssetCategoryResponse.from(child, null))
                        .collect(Collectors.toList()),
                iconFile,
                category.getAssets().stream().map(Asset::getId).collect(Collectors.toList()),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }

    public static AssetCategoryResponse fromWithoutChildren(AssetCategory category) {
        return new AssetCategoryResponse(
                category.getId(),
                category.getName(),
                category.getCode(),
                category.getParent() != null ? category.getParent().getId() : null,
                List.of(),
                null, // 서비스에서 FileService를 통해 설정할 예정
                category.getAssets().stream().map(Asset::getId).collect(Collectors.toList()),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }

    public static AssetCategoryResponse fromWithoutChildren(
            AssetCategory category, FileResponse iconFile) {
        return new AssetCategoryResponse(
                category.getId(),
                category.getName(),
                category.getCode(),
                category.getParent() != null ? category.getParent().getId() : null,
                List.of(),
                iconFile,
                category.getAssets().stream().map(Asset::getId).collect(Collectors.toList()),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
