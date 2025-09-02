package com.pluxity.facility.category.dto

import io.swagger.v3.oas.annotations.media.Schema

@JvmRecord
data class FacilityCategoryAllResponse(
    @field:Schema(description = "최대 depth", example = "3") @param:Schema(
        description = "최대 depth",
        example = "3"
    ) val maxDepth: Int,
    @JvmField @field:Schema(description = "카테고리 목록") @param:Schema(description = "카테고리 목록") val list: MutableList<FacilityCategoryResponse?>?
) {
    companion object {
        fun of(maxDepth: Int, list: MutableList<FacilityCategoryResponse?>?): FacilityCategoryAllResponse {
            return FacilityCategoryAllResponse(maxDepth, list)
        }
    }
}
