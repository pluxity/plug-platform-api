package com.pluxity.domains.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {

    @Bean
    public GroupedOpenApi deviceApi() {
        return GroupedOpenApi.builder()
                .group("99. 사상하단선 API")
                .pathsToMatch("/devices/**", "/device-categories/**", "/stations/**")
                .build();
    }
}
