package com.pluxity.user.dto

import com.pluxity.permission.dto.PermissionResponse
import com.pluxity.permission.dto.toPermissionResponse
import com.pluxity.user.entity.Role

data class RoleResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val permissions: List<PermissionResponse>,
)

fun Role.toRoleResponse() =
    RoleResponse(
        this.requiredId,
        this.name,
        this.description,
        this.rolePermissions
            .map { it.permission }
            .map { it.toPermissionResponse() }
            .toList(),
    )
