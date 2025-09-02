package com.pluxity.facility.category.dto

import io.swagger.v3.oas.annotations.media.Schema

data class FacilityCategoryAllResponse(
    @Schema(description = "최대 depth", example = "3")
    val maxDepth: Int,
    @Schema(description = "카테고리 목록")
    val list: List<FacilityCategoryResponse>,
)

fun List<FacilityCategoryResponse>.toAllResponse(maxDepth: Int): FacilityCategoryAllResponse =
    FacilityCategoryAllResponse(
        maxDepth = maxDepth,
        list = this,
    )
