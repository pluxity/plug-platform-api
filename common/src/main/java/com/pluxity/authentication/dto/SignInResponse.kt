package com.pluxity.authentication.dto

import lombok.Builder

@Builder
@JvmRecord
data class SignInResponse(val accessToken: String?, val name: String?, val code: String?)
