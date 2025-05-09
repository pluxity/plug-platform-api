package com.pluxity.facility.dto;

import jakarta.validation.constraints.NotBlank;

public record FloorRequest(
        @NotBlank
        String name,
        @NotBlank
        Integer groupId
) {
}
