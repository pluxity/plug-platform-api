package com.pluxity.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeviceCategoryUpdateRequest(
        @Size(max = 50, message = "카테고리 이름은 50자 이하여야 합니다.")
                @Schema(description = "카테고리 이름", example = "카테고리")
                @NotBlank
                String name,
        @Schema(description = "부모 카테고리 ID", example = "1") Long parentId,
        @Schema(description = "아이콘 파일 ID", example = "1") Long thumbnailFileId) {}
