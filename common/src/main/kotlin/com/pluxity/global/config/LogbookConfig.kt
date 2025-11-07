package com.pluxity.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zalando.logbook.HttpRequest
import org.zalando.logbook.Logbook
import org.zalando.logbook.core.Conditions
import java.util.function.Predicate
import kotlin.text.contains

@Configuration
class LogbookConfig {
    @Bean
    fun logbook(): Logbook {
        val excludePredicate: Predicate<HttpRequest> =
            Predicate { req ->
                val path = req.path
                path.contains("/actuator/") ||
                    path.contains("/swagger-ui/") ||
                    path.contains("/api-docs/") ||
                    path.contains("/.well-known/") ||
                    path.contains("/springwolf/")
            }
        val condition = Conditions.exclude(listOf(excludePredicate))
        return Logbook
            .builder()
            .condition(condition)
            .build()
    }
}
