package com.pluxity.authentication.dto

data class SignInResponse(
    val accessToken: String,
    val name: String,
    val code: String,
)
