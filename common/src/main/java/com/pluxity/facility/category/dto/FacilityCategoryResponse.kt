package com.pluxity.facility.category.dto

import com.pluxity.facility.category.FacilityCategory
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class FacilityCategoryResponse(
    @Schema(description = "카테고리 ID", example = "1")
    val id: Long,
    @Schema(description = "카테고리 이름", example = "그래픽 에셋")
    val name: String,
    @Schema(description = "부모 카테고리 ID", example = "2")
    val parentId: Long? = null,
    @Schema(
        description = "자식 카테고리 목록",
        example = "[]",
    )
    val children: MutableList<FacilityCategoryResponse> = mutableListOf(),
    @Schema(description = "생성일시")
    val createdAt: LocalDateTime,
    @Schema(description = "수정일시")
    val updatedAt: LocalDateTime,
    @Schema(description = "depth", example = "1")
    val depth: Int,
)

fun FacilityCategory.toResponse(): FacilityCategoryResponse =
    FacilityCategoryResponse(
        id = this.id!!,
        name = this.name,
        parentId = this.parent?.id,
        children = mutableListOf(),
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        depth = this.depth,
    )
