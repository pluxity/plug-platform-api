package com.pluxity.feature.dto;

import com.pluxity.feature.service.FeatureAssignType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record FeatureAssignDto(
        @Schema(description = "연결 대상 아이디", example = "id") @NotBlank(message = "연결 대상 아이디는 필수 입니다.")
                String id,
        @Schema(description = "연결 대상 타입") FeatureAssignType type) {}
