package com.pluxity.asset.dto

import com.pluxity.asset.entity.AssetCategory
import com.pluxity.file.dto.FileResponse
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class AssetCategoryResponse(
    @field:Schema(description = "카테고리 ID", example = "1")
    val id: Long?,
    @field:Schema(description = "카테고리 이름", example = "그래픽 에셋")
    val name: String?,
    @field:Schema(description = "카테고리 코드", example = "GRAPHIC_ASSET")
    val code: String?,
    @field:Schema(description = "부모 카테고리 ID", example = "2")
    val parentId: Long?,
    @field:Schema(description = "자식 카테고리 목록")
    val children: List<AssetCategoryResponse>,
    @field:Schema(description = "아이콘 파일 정보")
    val thumbnail: FileResponse?,
    @field:Schema(description = "소속 에셋 ID 목록")
    val assetIds: List<Long>,
    @field:Schema(description = "생성일시")
    val createdAt: LocalDateTime?,
    @field:Schema(description = "수정일시")
    val updatedAt: LocalDateTime?,
    @field:Schema(description = "depth", example = "1")
    val depth: Int,
) {
    companion object {
        @JvmStatic
        fun from(category: AssetCategory): AssetCategoryResponse =
            AssetCategoryResponse(
                id = category.id,
                name = category.name,
                code = category.code,
                parentId = category.parent?.id,
                children = category.children.map { from(it) },
                thumbnail = null,
                assetIds = category.assets.mapNotNull { it.id },
                createdAt = category.createdAt,
                updatedAt = category.updatedAt,
                depth = category.depth,
            )

        @JvmStatic
        fun from(
            category: AssetCategory,
            iconFile: FileResponse?,
        ): AssetCategoryResponse =
            AssetCategoryResponse(
                id = category.id,
                name = category.name,
                code = category.code,
                parentId = category.parent?.id,
                children = emptyList(),
                thumbnail = iconFile,
                assetIds = category.assets.mapNotNull { it.id },
                createdAt = category.createdAt,
                updatedAt = category.updatedAt,
                depth = category.depth,
            )

        @JvmStatic
        fun fromWithoutChildren(category: AssetCategory): AssetCategoryResponse =
            AssetCategoryResponse(
                id = category.id,
                name = category.name,
                code = category.code,
                parentId = category.parent?.id,
                children = emptyList(),
                thumbnail = null,
                assetIds = category.assets.mapNotNull { it.id },
                createdAt = category.createdAt,
                updatedAt = category.updatedAt,
                depth = category.depth,
            )

        @JvmStatic
        fun fromWithoutChildren(
            category: AssetCategory,
            iconFile: FileResponse?,
        ): AssetCategoryResponse =
            AssetCategoryResponse(
                id = category.id,
                name = category.name,
                code = category.code,
                parentId = category.parent?.id,
                children = emptyList(),
                thumbnail = iconFile,
                assetIds = category.assets.mapNotNull { it.id },
                createdAt = category.createdAt,
                updatedAt = category.updatedAt,
                depth = category.depth,
            )
    }
}
