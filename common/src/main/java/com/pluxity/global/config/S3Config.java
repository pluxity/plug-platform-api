package com.pluxity.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class S3Config {

    @Value("${file.s3.bucket}")
    private String bucketName;

    @Value("${file.s3.region}")
    private String region;

    @Value("${file.s3.endpoint-url}")
    private String endpointUrl;

    @Value("${file.s3.access-key}")
    private String accessKey;

    @Value("${file.s3.secret-key}")
    private String secretKey;

    @Value("${file.s3.pre-signed-url-expiration}")
    private int preSignedUrlExpiration;
}
