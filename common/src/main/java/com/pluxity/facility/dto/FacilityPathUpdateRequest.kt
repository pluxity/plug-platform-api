package com.pluxity.facility.dto

import io.swagger.v3.oas.annotations.media.Schema

@JvmRecord
data class FacilityPathUpdateRequest(
    @field:Schema(description = "이름", example = "이름") @param:Schema(
        description = "이름",
        example = "이름"
    ) val name: String?,
    @field:Schema(
        description = "타입",
        example = "SUBWAY(\"지하철\"), WAY(\"길찾기\"), PATROL(\"순찰\")"
    ) @param:Schema(description = "타입", example = "SUBWAY(\"지하철\"), WAY(\"길찾기\"), PATROL(\"순찰\")") val type: String?,
    @field:Schema(description = "경로정보", example = "[{\"id\"...}]") @param:Schema(
        description = "경로정보",
        example = "[{\"id\"...}]"
    ) val path: String?
)
