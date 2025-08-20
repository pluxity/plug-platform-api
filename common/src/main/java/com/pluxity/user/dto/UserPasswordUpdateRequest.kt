package com.pluxity.user.dto

data class UserPasswordUpdateRequest(
    val currentPassword: String,
    val newPassword: String,
)
