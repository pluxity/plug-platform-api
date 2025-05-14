package com.pluxity.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonApiConfig {

    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    public OpenAPI CommonOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Plug Platform API")
                                .description("Plug Platform API Documentation")
                                .version("1.0.0")
                                .contact(new Contact().name("Pluxity").email("support@pluxity.com"))
                                .license(
                                        new License()
                                                .name("Apache 2.0")
                                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    @Bean
    public GroupedOpenApi commonApi() {
        return GroupedOpenApi.builder().group("1. 전체").pathsToMatch("/**").build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("2. 인증")
                .pathsToMatch("/auth/**")
                .pathsToExclude("/users/**", "/admin/**", "/other/**") // 제외 경로 추가
                .build();
    }

    @Bean
    public GroupedOpenApi fileApiByPath() {
        return GroupedOpenApi.builder().group("3. 파일관리 API").pathsToMatch("/files/**").build();
    }

    @Bean
    public GroupedOpenApi userApiByPath() {
        return GroupedOpenApi.builder()
                .group("4. 사용자 API")
                .pathsToMatch("/users/**", "/admin/users/**", "/roles/**")
                .build();
    }

    @Bean
    public GroupedOpenApi facilityApiByPath() {
        return GroupedOpenApi.builder()
                .group("5. 시설관리 API")
                .pathsToMatch("/facilities/**", "/buildings/**", "/stations/**", "/panoramas/**")
                .build();
    }

    @Bean
    public GroupedOpenApi assetApiByPath() {
        return GroupedOpenApi.builder()
                .group("6. Asset 관리 API")
                .pathsToMatch("/assets/**")
                .build();
    }
}
