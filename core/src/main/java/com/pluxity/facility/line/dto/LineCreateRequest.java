package com.pluxity.facility.line.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LineCreateRequest(
        @Schema(description = "노선 이름", example = "1호선", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "노선 이름은 필수입니다")
                @Size(max = 50, message = "노선 이름은 50자 이하여야 합니다")
                String name,
        String color) {}
