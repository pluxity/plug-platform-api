package com.pluxity.domains.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NfluxCreateRequest(
        @NotNull(message = "Device ID는 필수 입니다.") @NotBlank(message = "Device ID는 비어있을 수 없습니다.")
                String id,
        Long deviceCategoryId,
        Long asset,
        String name) {}
