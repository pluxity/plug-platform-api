package com.pluxity.user.dto

import com.pluxity.user.entity.User

data class UserResponse(
    val id: Long,
    val username: String,
    val name: String,
    val code: String?,
    val phoneNumber: String?,
    val department: String?,
    val shouldChangePassword: Boolean,
    val roles: List<RoleResponse>,
)

fun User.toUserResponse(): UserResponse =
    UserResponse(
        id = this.id!!,
        username = this.username,
        name = this.name,
        code = this.code,
        phoneNumber = this.phoneNumber,
        department = this.department,
        shouldChangePassword = this.isPasswordChangeRequired(),
        roles = this.userRoles.mapNotNull { RoleResponse.from(it.role) },
    )
