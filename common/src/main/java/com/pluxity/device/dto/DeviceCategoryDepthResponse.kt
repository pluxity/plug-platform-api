package com.pluxity.device.dto

import io.swagger.v3.oas.annotations.media.Schema

data class DeviceCategoryDepthResponse(
    @field:Schema(
        description = "최대 depth",
        example = "3",
    )
    val maxDepth: Int,
)
