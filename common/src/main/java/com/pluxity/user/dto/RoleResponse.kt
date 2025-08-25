package com.pluxity.user.dto

import com.pluxity.permission.dto.PermissionGroupResponse
import com.pluxity.permission.dto.toPermissionGroupResponse
import com.pluxity.user.entity.Role

data class RoleResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val permissions: List<PermissionGroupResponse>,
)

fun Role.toRoleResponse() =
    RoleResponse(
        this.id!!,
        this.name,
        this.description,
        this.rolePermissions
            .map { it.permissionGroup }
            .map { it.toPermissionGroupResponse() }
            .toList(),
    )
