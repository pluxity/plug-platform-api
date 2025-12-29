package com.pluxity.permission.dto

import com.pluxity.permission.PermissionLevel

data class PermissionItemResponse(
    val resourceId: String,
    val level: PermissionLevel,
)

data class PermissionResponse(
    val resourceType: String,
    val permissions: List<PermissionItemResponse>,
)
