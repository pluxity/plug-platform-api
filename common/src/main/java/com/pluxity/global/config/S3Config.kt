package com.pluxity.global.config

import lombok.Getter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Getter
@Configuration
class S3Config {
    @Value("\${file.s3.bucket}")
    lateinit var bucketName: String

    @Value("\${file.s3.region}")
    lateinit var region: String

    @Value("\${file.s3.endpoint-url}")
    lateinit var endpointUrl: String

    @Value("\${file.s3.access-key}")
    lateinit var accessKey: String

    @Value("\${file.s3.secret-key}")
    lateinit var secretKey: String

    @Value("\${file.s3.pre-signed-url-expiration}")
    val preSignedUrlExpiration = 0
}
