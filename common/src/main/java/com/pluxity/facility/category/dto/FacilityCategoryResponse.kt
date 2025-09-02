package com.pluxity.facility.category.dto

import com.pluxity.facility.category.FacilityCategory
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JvmRecord
data class FacilityCategoryResponse(
    @field:Schema(description = "카테고리 ID", example = "1") @param:Schema(
        description = "카테고리 ID",
        example = "1"
    ) val id: Long?,
    @field:Schema(description = "카테고리 이름", example = "그래픽 에셋") @param:Schema(
        description = "카테고리 이름",
        example = "그래픽 에셋"
    ) val name: String?,
    @field:Schema(description = "부모 카테고리 ID", example = "2") @param:Schema(
        description = "부모 카테고리 ID",
        example = "2"
    ) val parentId: Long?,
    @field:Schema(
        description = "자식 카테고리 목록",
        example = "[{\"id\":2,\"name\":\"서브 카테고리\",\"parentId\":1,\"children\":[],\"createdAt\":\"string\",\"updatedAt\":\"string\",\"depth\":2}]"
    ) @param:Schema(
        description = "자식 카테고리 목록",
        example = "[{\"id\":2,\"name\":\"서브 카테고리\",\"parentId\":1,\"children\":[],\"createdAt\":\"string\",\"updatedAt\":\"string\",\"depth\":2}]"
    ) val children: MutableList<FacilityCategoryResponse?>?,
    @field:Schema(description = "생성일시") @param:Schema(description = "생성일시") val createdAt: LocalDateTime?,
    @field:Schema(description = "수정일시") @param:Schema(description = "수정일시") val updatedAt: LocalDateTime?,
    @field:Schema(description = "depth", example = "1") @param:Schema(
        description = "depth",
        example = "1"
    ) val depth: Int
) {
    companion object {
        fun from(category: FacilityCategory): FacilityCategoryResponse {
            return FacilityCategoryResponse(
                category.id,
                category.name,
                if (category.parent != null) category.parent!!.id else null,
                ArrayList<FacilityCategoryResponse?>(),
                category.createdAt,
                category.updatedAt,
                category.depth
            )
        }
    }
}
