package com.pluxity.permission.dto

import io.swagger.v3.oas.annotations.media.Schema

data class PermissionGroupUpdateRequest(
    @field:Schema(description = "권한 집합 이름")
    val name: String?,
    @field:Schema(description = "권한에 대한 설명")
    val description: String?,
    @field:Schema(description = "권한의 상세 목록")
    val permissions: List<PermissionRequest>,
)
