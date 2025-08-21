package com.pluxity.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DeviceCategoryDepthResponse(
        @Schema(description = "최대 depth", example = "3") int maxDepth) {}
