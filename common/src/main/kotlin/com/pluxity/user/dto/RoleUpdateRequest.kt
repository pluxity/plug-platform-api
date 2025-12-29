package com.pluxity.user.dto

import com.pluxity.permission.ResourceType
import io.swagger.v3.oas.annotations.media.Schema

data class RoleUpdateRequest(
    @field:Schema(description = "역할 이름", defaultValue = "역할 이름")
    val name: String?,
    @field:Schema(description = "역할 설명", defaultValue = "역할 설명")
    val description: String?,
    @field:Schema(description = "권한 아이디")
    val permissionGroupIds: List<Long>?,
    @field:Schema(description = "등록 권한 리소스 타입 목록")
    val globalPolicyTypes: List<ResourceType>?,
)
