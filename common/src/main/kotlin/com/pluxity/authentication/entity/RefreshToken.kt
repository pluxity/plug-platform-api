package com.pluxity.authentication.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed

@RedisHash("refresh_token")
data class RefreshToken(
    @Id val username: String,
    @Indexed val token: String,
    @TimeToLive val timeToLive: Int,
) {
    fun isValidToken(): Boolean = token.isNotBlank()
}
