package com.pluxity.domains.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NfluxCreateRequest(
        Long deviceCategoryId,
        Long asset,
        String name,
        @NotNull(message = "Device code는 필수 입니다.") @NotBlank(message = "Device code는 비어있을 수 없습니다.")
                String code,
        String description) {}
