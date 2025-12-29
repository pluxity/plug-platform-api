package com.pluxity.user.dto

import com.pluxity.permission.dto.PermissionGroupResponse
import com.pluxity.permission.dto.toPermissionGroupResponse
import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.Role

data class RoleResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val permissions: List<PermissionGroupResponse>,
    val globalPolicyTypes: List<ResourceType>,
)

fun Role.toRoleResponse() =
    RoleResponse(
        this.requiredId,
        this.name,
        this.description,
        this.rolePermissions
            .map { it.permissionGroup }
            .map { it.toPermissionGroupResponse() }
            .toList(),
        this.roleGlobalPolicies
            .map { it.resourceType }
            .distinct(),
    )
