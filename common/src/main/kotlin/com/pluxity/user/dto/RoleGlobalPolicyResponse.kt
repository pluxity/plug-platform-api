package com.pluxity.user.dto

import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.RoleGlobalPermissionType
import com.pluxity.user.entity.RoleGlobalPolicy

data class RoleGlobalPolicyResponse(
    val resourceType: ResourceType,
    val permissionType: RoleGlobalPermissionType,
)

fun RoleGlobalPolicy.toRoleGlobalPolicyResponse() =
    RoleGlobalPolicyResponse(
        resourceType = resourceType,
        permissionType = permissionType,
    )
