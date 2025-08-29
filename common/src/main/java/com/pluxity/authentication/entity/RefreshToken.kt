package com.pluxity.authentication.entity

import com.pluxity.user.entity.User.username
import lombok.*
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@RedisHash("refresh_token")
class RefreshToken {
    @Id
    private var username: String? = null

    @Indexed
    private var token: String? = null

    @TimeToLive
    private var timeToLive = 0

    companion object {
        @JvmStatic
        fun of(username: String?, token: String?, timeToLive: Int): RefreshToken? {
            return RefreshToken.builder().username(username).token(token).timeToLive(timeToLive).build()
        }
    }
}
