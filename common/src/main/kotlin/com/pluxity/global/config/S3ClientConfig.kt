package com.pluxity.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Configuration
class S3ClientConfig(
    private val s3Config: S3Config,
) {
    @Bean
    fun s3Client(): S3Client =
        S3Client
            .builder()
            .region(Region.of(s3Config.region))
            .endpointOverride(URI.create(s3Config.endpointUrl))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(s3Config.accessKey, s3Config.secretKey),
                ),
            ).forcePathStyle(true)
            .build()
}
