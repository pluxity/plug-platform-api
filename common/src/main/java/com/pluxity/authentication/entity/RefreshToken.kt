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
    companion object {
        @JvmStatic
        fun of(
            username: String,
            token: String,
            timeToLive: Int,
        ): RefreshToken = RefreshToken(username, token, timeToLive)
    }
}
