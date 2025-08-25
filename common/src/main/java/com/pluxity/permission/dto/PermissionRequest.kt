package com.pluxity.permission.dto

import com.pluxity.permission.ResourceType
import io.swagger.v3.oas.annotations.media.Schema

data class PermissionRequest(
    @field:Schema(
        description = "자원 유형",
        implementation = ResourceType::class,
        example = "FACILITY",
    ) val resourceType: String,
    @field:Schema(
        description = "자원 아이디",
    ) val resourceIds: List<String>,
)
