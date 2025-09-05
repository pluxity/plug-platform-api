package com.pluxity.user.dto

data class UserRoleUpdateRequest(
    val roleIds: List<Long> = emptyList(),
)
