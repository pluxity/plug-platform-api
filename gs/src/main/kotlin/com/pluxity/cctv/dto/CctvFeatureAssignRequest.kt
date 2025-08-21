package com.pluxity.cctv.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

class CctvFeatureAssignRequest(
    @field:Schema(
        description = "Feature ID",
        example = "id",
    )
    @field:NotBlank(message = "Feature ID는 필수 입니다.")
    var featureId: String,
)
