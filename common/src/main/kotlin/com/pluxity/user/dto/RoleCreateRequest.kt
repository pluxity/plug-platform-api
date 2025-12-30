package com.pluxity.user.dto

import com.pluxity.user.entity.RoleType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class RoleCreateRequest(
    @field:NotBlank(message = "Role name cannot be empty")
    @field:Schema(description = "역할 이름", defaultValue = "역할 이름")
    val name: String,
    @field:Schema(description = "역할 설명", defaultValue = "역할 설명")
    val description: String?,
    @field:Schema(description = "권한 아이디")
    val permissionGroupIds: List<Long> = emptyList(),
    @field:Schema(description = "등록 권한 정책 목록")
    val globalPolicies: List<RoleGlobalPolicyRequest> = emptyList(),
    @field:Schema(description = "사용자 권한", defaultValue = "USER")
    val authority: RoleType = RoleType.USER,
)
