package com.pluxity.onboarding.dto

data class AdminUserCreateRequest (
    val username: String,
    val password: String,
    val name: String
)