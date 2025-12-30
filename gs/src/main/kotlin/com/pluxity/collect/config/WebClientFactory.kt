
package com.pluxity.collect.config

import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.netty.http.client.HttpClient
import java.time.Duration

private val log = KotlinLogging.logger {}

@Component
class WebClientFactory(
    private val webClientBuilder: WebClient.Builder,
) {
    fun createClient(
        baseUrl: String,
        connectionTimeoutMs: Int = 5000,
        responseTimeoutMs: Int = 30000,
        readTimeoutMs: Int = 30000,
    ): WebClient {
        val httpClient =
            HttpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutMs)
                .responseTimeout(Duration.ofMillis(responseTimeoutMs.toLong()))
                .doOnConnected { conn ->
                    conn
                        .addHandlerLast(ReadTimeoutHandler(readTimeoutMs / 1000))
                        .addHandlerLast(WriteTimeoutHandler(readTimeoutMs / 1000))
                }

        return webClientBuilder
            .clone()
            .baseUrl(baseUrl)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .filter { request, next ->
                next
                    .exchange(request)
                    .doOnError { error ->
                        log.error(error) {
                            "WebClient 요청 실패 - URL: ${request.url()}, Method: ${request.method()}, Headers: ${request.headers()}"
                        }
                    }.onErrorMap { error ->
                        when (error) {
                            is WebClientResponseException -> {
                                log.error {
                                    "HTTP 에러 응답 - Status: ${error.statusCode}, Body: ${error.responseBodyAsString}, URL: ${request.url()}"
                                }
                                error
                            }
                            is WebClientRequestException -> {
                                log.error(error) {
                                    "WebClient 요청 실패 - URL: ${request.url()}, 원인: ${error.message}"
                                }
                                error
                            }
                            else -> {
                                log.error(error) {
                                    "예상치 못한 에러 - URL: ${request.url()}, 원인: ${error.message}"
                                }
                                error
                            }
                        }
                    }
            }.build()
    }
}
