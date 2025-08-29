package com.pluxity.authentication.dto

import lombok.Builder

@Builder
@JvmRecord
data class TokenResponse(val accessToken: String?)
