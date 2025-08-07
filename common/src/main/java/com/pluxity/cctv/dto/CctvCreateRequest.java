package com.pluxity.cctv.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CctvCreateRequest(
        @Schema(description = "CCTV ID", example = "id")
                @NotBlank(message = "ID는 필수 입니다.")
                @Size(max = 50, message = "ID는 최대 50자까지 입력 가능합니다.")
                String id,
        @Schema(description = "CCTV 이름", example = "cctv1")
                @NotBlank(message = "CCTV 이름은 필수 입니다.")
                @Size(max = 50, message = "CCTV 이름은 최대 50자까지 입력 가능합니다.")
                String name,
        @Schema(description = "CCTV URL", example = "rtsp://example.com/stream")
                @Size(max = 1000, message = "CCTV URL은 최대 1000자까지 입력 가능합니다.")
                String url,
        @Schema(description = "CCTV 카테고리 ID", example = "1") Long categoryId) {}
