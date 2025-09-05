package com.pluxity.feature.dto

import com.pluxity.feature.service.FeatureAssignType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class FeatureAssignDto(
    @field:Schema(
        description = "연결 대상 아이디",
        example = "id",
    )
    @field:NotBlank(message = "연결 대상 아이디는 필수 입니다.")
    val id: String,
    @field:Schema(description = "연결 대상 타입")
    val type: FeatureAssignType,
)
