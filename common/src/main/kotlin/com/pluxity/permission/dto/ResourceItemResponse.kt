package com.pluxity.permission.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "리소스 항목 정보")
data class ResourceItemResponse(
    @field:Schema(description = "리소스 ID", example = "1")
    val id: String,
    @field:Schema(description = "리소스 이름", example = "1번 CCTV")
    val name: String,
)
