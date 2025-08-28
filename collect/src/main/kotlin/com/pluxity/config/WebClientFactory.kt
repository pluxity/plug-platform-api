package com.pluxity.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

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
            .build()
    }
}
