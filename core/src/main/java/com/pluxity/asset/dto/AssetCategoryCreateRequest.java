package com.pluxity.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssetCategoryCreateRequest(
        @Schema(description = "카테고리 이름", example = "그래픽 에셋")
                @NotBlank(message = "카테고리 이름은 필수입니다")
                @Size(max = 100, message = "카테고리 이름은 100자를 초과할 수 없습니다")
                String name,
        @Schema(description = "카테고리 코드", example = "GRAPHIC_ASSET")
                @NotBlank(message = "카테고리 코드는 필수입니다")
                @Size(max = 3, message = "카테고리 코드는 3글자를 초과할 수 없습니다")
                String code,
        @Schema(description = "부모 카테고리 ID", example = "1") Long parentId,
        @Schema(description = "아이콘 파일 ID", example = "10") Long iconFileId) {}
