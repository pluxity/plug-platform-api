package com.pluxity.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Core Module API")
                .version("v1.0")
                .description("OAS Documentation");

        return new OpenAPI()
                .info(info);
    }
}