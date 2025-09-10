package com.pluxity.global.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "user")
data class UserProperties
    @ConstructorBinding
    constructor(
        val initPassword: String,
    )
