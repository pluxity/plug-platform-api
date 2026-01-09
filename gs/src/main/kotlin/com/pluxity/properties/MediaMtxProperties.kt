package com.pluxity.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mediamtx")
data class MediaMtxProperties(
    val apiUrl: String,
    val viewUrl: String,
)
