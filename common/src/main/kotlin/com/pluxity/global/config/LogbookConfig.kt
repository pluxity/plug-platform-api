package com.pluxity.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zalando.logbook.HttpRequest
import org.zalando.logbook.Logbook
import org.zalando.logbook.Sink
import org.zalando.logbook.core.Conditions
import org.zalando.logbook.core.DefaultHttpLogWriter
import org.zalando.logbook.core.DefaultSink
import org.zalando.logbook.core.HeaderFilters
import org.zalando.logbook.json.JacksonJsonFieldBodyFilter
import org.zalando.logbook.json.JsonHttpLogFormatter
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

        // JSON 한 줄 포맷터 + 기본 SLF4J writer
        val sink: Sink =
            DefaultSink(
                JsonHttpLogFormatter(), // 한 줄짜리 JSON 포맷
                DefaultHttpLogWriter(), // SLF4J 로 로그 찍는 writer
            )

        return Logbook
            .builder()
            .condition(condition)
            .headerFilter(
                HeaderFilters.replaceHeaders(
                    { name, _ ->
                        name.equals("Cookie", ignoreCase = true) ||
                            name.equals("Set-Cookie", ignoreCase = true)
                    },
                    "<obfuscated>",
                ),
            ).bodyFilter(
                JacksonJsonFieldBodyFilter(
                    listOf("password"),
                    "<obfuscated>",
                ),
            ).sink(sink)
            .build()
    }
}
