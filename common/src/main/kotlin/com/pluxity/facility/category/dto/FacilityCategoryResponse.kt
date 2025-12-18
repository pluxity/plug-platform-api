package com.pluxity.facility.category.dto

import com.pluxity.facility.category.FacilityCategory
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class FacilityCategoryResponse(
    @field:Schema(description = "카테고리 ID", example = "1")
    val id: Long,
    @field:Schema(description = "카테고리 이름", example = "그래픽 에셋")
    val name: String,
    @field:Schema(description = "부모 카테고리 ID", example = "2")
    val parentId: Long? = null,
    @field:Schema(
        description = "자식 카테고리 목록",
        example = "[]",
    )
    val children: MutableList<FacilityCategoryResponse> = mutableListOf(),
    @field:Schema(description = "생성일시")
    val createdAt: LocalDateTime,
    @field:Schema(description = "수정일시")
    val updatedAt: LocalDateTime,
    @field:Schema(description = "depth", example = "1")
    val depth: Int,
)

fun FacilityCategory.toResponse(): FacilityCategoryResponse =
    FacilityCategoryResponse(
        id = this.requiredId(),
        name = this.name,
        parentId = this.parent?.id,
        children = mutableListOf(),
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        depth = this.depth,
    )

fun FacilityCategory.toResponseWithChildren(): FacilityCategoryResponse =
    this.toResponse().copy(
        children =
            this.children
                .map {
                    it.toResponseWithChildren()
                }.toMutableList(),
    )
