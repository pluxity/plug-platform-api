package com.pluxity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SasangApplication {
    public static void main(String[] args) {
        SpringApplication.run(SasangApplication.class, args);
    }
}
