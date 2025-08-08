package com.pluxity.cctv.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CctvAssignDto(
        @Schema(description = "CCTV ID", example = "id") @NotBlank(message = "ID는 필수 입니다.")
                String id) {}
