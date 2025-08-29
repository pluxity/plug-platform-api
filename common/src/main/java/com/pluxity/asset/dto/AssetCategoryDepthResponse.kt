package com.pluxity.asset.dto

import io.swagger.v3.oas.annotations.media.Schema

data class AssetCategoryDepthResponse(
    @field:Schema(description = "최대 depth", example = "3")
    val maxDepth: Int,
)
