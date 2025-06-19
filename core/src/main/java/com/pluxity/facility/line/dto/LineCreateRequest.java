package com.pluxity.facility.line.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LineCreateRequest(
    @Schema(description = "노선 이름", example = "1호선", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Line name cannot be blank.") // Updated message
        @Size(max = 50, message = "Line name must be less than or equal to 50 characters.")
        String name,

    @Schema(description = "노선 색상", example = "#FF0000") // Added Schema for consistency
        @Size(max = 255, message = "Color string must be less than or equal to 255 characters.") // Added validation
        String color
) {}
