package com.pluxity.authentication.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@RedisHash("refresh_token")
public class RefreshToken {

    @Id private String username;

    @Indexed private String token;

    @TimeToLive private int timeToLive;

    public static RefreshToken of(String username, String token, int timeToLive) {
        return RefreshToken.builder()
                .username(username)
                .token(token)
                .timeToLive(timeToLive)
                .build();
    }
}
