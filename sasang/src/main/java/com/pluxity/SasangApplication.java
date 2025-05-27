package com.pluxity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = com.pluxity.global.config.CommonSecurityConfig.class)
        })
public class SasangApplication {
    public static void main(String[] args) {
        SpringApplication.run(SasangApplication.class, args);
    }
}
