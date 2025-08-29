package com.pluxity.asset.dto

import io.swagger.v3.oas.annotations.media.Schema

@JvmRecord
data class AssetCategoryDepthResponse(
    @field:Schema(description = "최대 depth", example = "3") @param:Schema(
        description = "최대 depth",
        example = "3"
    ) val maxDepth: Int
)
