package com.pluxity.global.config

import com.pluxity.global.properties.S3Properties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.core.interceptor.Context
import software.amazon.awssdk.core.interceptor.ExecutionAttributes
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor
import software.amazon.awssdk.http.SdkHttpRequest
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Configuration
class S3ClientConfig(
    private val s3Properties: S3Properties,
) {
    @Bean
    fun s3Client(): S3Client =
        S3Client
            .builder()
            .region(Region.of(s3Properties.region))
            .endpointOverride(URI.create(s3Properties.endpointUrl))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(s3Properties.accessKey, s3Properties.secretKey),
                ),
            ).forcePathStyle(true)
            .overrideConfiguration(
                ClientOverrideConfiguration
                    .builder()
                    .addExecutionInterceptor(PinpointHeaderRemoveInterceptor())
                    .build(),
            ).build()
}

private class PinpointHeaderRemoveInterceptor : ExecutionInterceptor {
    companion object {
        private const val PINPOINT_HEADER_PREFIX = "Pinpoint-"
    }

    override fun modifyHttpRequest(
        context: Context.ModifyHttpRequest,
        executionAttributes: ExecutionAttributes,
    ): SdkHttpRequest {
        val request = context.httpRequest()
        val filteredHeaders =
            request
                .headers()
                .filter { (key, _) -> !key.startsWith(PINPOINT_HEADER_PREFIX) }
                .toMap()

        return request.toBuilder().headers(filteredHeaders).build()
    }
}
