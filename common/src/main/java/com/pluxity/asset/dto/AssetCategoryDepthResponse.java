package com.pluxity.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AssetCategoryDepthResponse(
        @Schema(description = "최대 depth", example = "3") int maxDepth) {}
