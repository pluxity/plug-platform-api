package com.pluxity.global.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "spring.data.redis")
class RedisProperties
    @ConstructorBinding
    constructor(
        val host: String,
        val port: Int = 0,
    )
