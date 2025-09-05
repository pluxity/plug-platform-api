package com.pluxity.category.dto

import io.swagger.v3.oas.annotations.media.Schema

data class CategoryDepthResponse(
    @field:Schema(
        description = "최대 depth",
        example = "3",
    )
    val maxDepth: Int,
)
