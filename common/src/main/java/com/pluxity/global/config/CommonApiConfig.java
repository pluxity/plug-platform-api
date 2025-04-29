package com.pluxity.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonApiConfig {

    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    public OpenAPI CommonOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Plug Platform API")
                        .description("Plug Platform API Documentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Pluxity")
                                .email("support@pluxity.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
} 