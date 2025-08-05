package com.pluxity.facility.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record FacilityCategoryUpdateRequest(
        @Schema(
                        description = "카테고리 이름",
                        example = "그래픽 에셋",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @Size(max = 100, message = "카테고리 이름은 100자를 초과할 수 없습니다")
                String name,
        @Schema(description = "부모 카테고리 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
                Long parentId) {}
