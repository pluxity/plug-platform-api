package com.pluxity.permission.dto

data class PermissionResponse(
    val resourceType: String,
    val resourceIds: List<String>,
)
