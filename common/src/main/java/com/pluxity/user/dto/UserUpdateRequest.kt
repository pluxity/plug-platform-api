package com.pluxity.user.dto

data class UserUpdateRequest(
    val name: String? = null,
    val code: String? = null,
    val phoneNumber: String? = null,
    val department: String? = null,
    val roleIds: List<Long>? = null,
)
