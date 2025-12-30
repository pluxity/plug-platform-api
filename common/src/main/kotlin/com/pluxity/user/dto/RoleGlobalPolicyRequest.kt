package com.pluxity.user.dto

import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.RoleGlobalPermissionType

data class RoleGlobalPolicyRequest(
    val resourceType: ResourceType,
    val permissionType: RoleGlobalPermissionType,
)
