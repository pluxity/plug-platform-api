package com.pluxity.asset.dto

import com.pluxity.asset.entity.Asset
import com.pluxity.asset.entity.AssetCategory
import com.pluxity.file.dto.FileResponse
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.stream.Collectors

@JvmRecord
data class AssetCategoryResponse(
    @field:Schema(description = "카테고리 ID", example = "1") @param:Schema(
        description = "카테고리 ID",
        example = "1"
    ) val id: Long?,
    @field:Schema(description = "카테고리 이름", example = "그래픽 에셋") @param:Schema(
        description = "카테고리 이름",
        example = "그래픽 에셋"
    ) val name: String?,
    @field:Schema(description = "카테고리 코드", example = "GRAPHIC_ASSET") @param:Schema(
        description = "카테고리 코드",
        example = "GRAPHIC_ASSET"
    ) val code: String?,
    @JvmField @field:Schema(description = "부모 카테고리 ID", example = "2") @param:Schema(
        description = "부모 카테고리 ID",
        example = "2"
    ) val parentId: Long?,
    @JvmField @field:Schema(
        description = "자식 카테고리 목록",
        example = "[{\"id\":2,\"name\":\"서브 카테고리\",\"code\":\"SUB\",\"parentId\":1,\"children\":[],\"thumbnail\":null,\"assetIds\":[0],\"createdAt\":\"string\",\"updatedAt\":\"string\",\"depth\":2}]"
    ) @param:Schema(
        description = "자식 카테고리 목록",
        example = "[{\"id\":2,\"name\":\"서브 카테고리\",\"code\":\"SUB\",\"parentId\":1,\"children\":[],\"thumbnail\":null,\"assetIds\":[0],\"createdAt\":\"string\",\"updatedAt\":\"string\",\"depth\":2}]"
    ) val children: MutableList<AssetCategoryResponse?>?,
    @field:Schema(description = "아이콘 파일 정보") @param:Schema(description = "아이콘 파일 정보") val thumbnail: FileResponse?,
    @field:Schema(description = "소속 에셋 ID 목록") @param:Schema(description = "소속 에셋 ID 목록") val assetIds: MutableList<Long?>?,
    @field:Schema(description = "생성일시") @param:Schema(description = "생성일시") val createdAt: LocalDateTime?,
    @field:Schema(description = "수정일시") @param:Schema(description = "수정일시") val updatedAt: LocalDateTime?,
    @JvmField @field:Schema(description = "depth", example = "1") @param:Schema(
        description = "depth",
        example = "1"
    ) val depth: Int
) {
    companion object {
        fun from(category: AssetCategory): AssetCategoryResponse {
            return AssetCategoryResponse(
                category.getId(),
                category.getName(),
                category.getCode(),
                if (category.getParent() != null) category.getParent().getId() else null,
                category.getChildren().stream()
                    .map<AssetCategoryResponse?> { category: AssetCategory? -> Companion.from(category!!) }
                    .collect(Collectors.toList()),
                null,  // 서비스에서 FileService를 통해 설정할 예정
                category.getAssets().stream().map<Long?> { obj: Asset? -> obj!!.getId() }.collect(Collectors.toList()),
                category.getCreatedAt(),
                category.getUpdatedAt(),
                category.getDepth()
            )
        }

        fun from(category: AssetCategory, iconFile: FileResponse?): AssetCategoryResponse {
            return AssetCategoryResponse(
                category.getId(),
                category.getName(),
                category.getCode(),
                if (category.getParent() != null) category.getParent().getId() else null,
                ArrayList<AssetCategoryResponse?>(),
                iconFile,
                category.getAssets().stream().map<Long?> { obj: Asset? -> obj!!.getId() }.collect(Collectors.toList()),
                category.getCreatedAt(),
                category.getUpdatedAt(),
                category.getDepth()
            )
        }

        fun fromWithoutChildren(category: AssetCategory): AssetCategoryResponse {
            return AssetCategoryResponse(
                category.getId(),
                category.getName(),
                category.getCode(),
                if (category.getParent() != null) category.getParent().getId() else null,
                mutableListOf<AssetCategoryResponse?>(),
                null,  // 서비스에서 FileService를 통해 설정할 예정
                category.getAssets().stream().map<Long?> { obj: Asset? -> obj!!.getId() }.collect(Collectors.toList()),
                category.getCreatedAt(),
                category.getUpdatedAt(),
                category.getDepth()
            )
        }

        fun fromWithoutChildren(
            category: AssetCategory, iconFile: FileResponse?
        ): AssetCategoryResponse {
            return AssetCategoryResponse(
                category.getId(),
                category.getName(),
                category.getCode(),
                if (category.getParent() != null) category.getParent().getId() else null,
                mutableListOf<AssetCategoryResponse?>(),
                iconFile,
                category.getAssets().stream().map<Long?> { obj: Asset? -> obj!!.getId() }.collect(Collectors.toList()),
                category.getCreatedAt(),
                category.getUpdatedAt(),
                category.getDepth()
            )
        }
    }
}
