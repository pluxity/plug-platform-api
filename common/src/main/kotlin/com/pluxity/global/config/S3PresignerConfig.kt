package com.pluxity.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@Configuration
class S3PresignerConfig(
    private val s3Config: S3Config,
) {
    @Bean
    fun s3Presigner(): S3Presigner =
        S3Presigner
            .builder()
            .region(Region.of(s3Config.region))
            .endpointOverride(URI.create(s3Config.endpointUrl))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(s3Config.accessKey, s3Config.secretKey),
                ),
            ).build()
}
