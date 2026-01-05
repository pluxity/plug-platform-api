package com.pluxity.permission.dto

import com.pluxity.permission.PermissionLevel

data class ResourcePermissionItemResponse(
    val resourceId: String,
    val level: PermissionLevel,
)

data class ResourcePermissionResponse(
    val resourceType: String,
    val permissions: List<ResourcePermissionItemResponse>,
)

data class DomainPermissionResponse(
    val resourceType: String,
    val level: PermissionLevel,
)
