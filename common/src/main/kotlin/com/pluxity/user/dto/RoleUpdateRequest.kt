package com.pluxity.user.dto

data class RoleUpdateRequest(
    val name: String?,
    val description: String?,
    val permissionGroupIds: List<Long>?,
)
