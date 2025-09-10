package com.pluxity.global.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "file")
data class FileProperties
    @ConstructorBinding
    constructor(
        val storageStrategy: String,
        val local: LocalProperties,
    )

data class LocalProperties(
    val path: String,
)

@ConfigurationProperties(prefix = "file.s3")
data class S3Properties
    @ConstructorBinding
    constructor(
        val bucket: String,
        val region: String,
        val endpointUrl: String,
        val publicUrl: String,
        val accessKey: String,
        val secretKey: String,
        val preSignedUrlExpiration: Int = 0,
    )
