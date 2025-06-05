package com.pluxity.asset.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssetUpdateRequest(
        @Schema(description = "에셋 이름", example = "로고 이미지")
                @Size(max = 50, message = "에셋 이름은 50자를 초과할 수 없습니다")
                @NotBlank(message = "에셋 이름은 필수입니다")
                String name,
        @Schema(description = "에셋 코드", example = "LOGO_IMAGE")
                @Size(max = 10, message = "에셋 코드는 10자를 초과할 수 없습니다")
                @NotBlank(message = "에셋 코드는 필수입니다")
                String code,
        @Schema(description = "파일 ID", example = "1") Long fileId,
        @Schema(description = "썸네일 파일 ID", example = "2") Long thumbnailFileId,
        @Schema(description = "카테고리 ID", example = "3") Long categoryId) {}
