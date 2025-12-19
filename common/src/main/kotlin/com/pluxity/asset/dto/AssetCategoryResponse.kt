package com.pluxity.asset.dto

import com.pluxity.asset.entity.AssetCategory
import com.pluxity.file.dto.FileResponse
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class AssetCategoryResponse(
    @field:Schema(description = "카테고리 ID", example = "1")
    val id: Long?,
    @field:Schema(description = "카테고리 이름", example = "그래픽 에셋")
    val name: String,
    @field:Schema(description = "카테고리 코드", example = "GRAPHIC_ASSET")
    val code: String?,
    @field:Schema(description = "부모 카테고리 ID", example = "2")
    val parentId: Long?,
    @field:Schema(description = "자식 카테고리 목록")
    val children: MutableList<AssetCategoryResponse> = mutableListOf(),
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
)

fun AssetCategory.toResponse(
    includeChildren: Boolean = true,
    thumbnailFile: FileResponse? = null,
): AssetCategoryResponse =
    AssetCategoryResponse(
        id = this.requiredId,
        name = this.name,
        code = this.code,
        parentId = this.parent?.id,
        children =
            if (includeChildren) {
                this.children.map { it.toResponse(includeChildren = true, thumbnailFile = null) }.toMutableList()
            } else {
                mutableListOf()
            },
        thumbnail = thumbnailFile,
        assetIds = this.assets.mapNotNull { it.id },
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        depth = this.depth,
    )

fun AssetCategory.toAssetCategoryResponseWithChildren(fileMap: Map<Long, FileResponse>): AssetCategoryResponse =
    this.toResponse(includeChildren = true, thumbnailFile = fileMap[this.iconFileId] ?: FileResponse()).copy(
        children =
            this.children
                .map { it.toAssetCategoryResponseWithChildren(fileMap) }
                .toMutableList(),
    )
