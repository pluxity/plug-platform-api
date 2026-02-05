package com.pluxity.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.util.concurrent.Executor

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val heartBeatScheduler: TaskScheduler,
    private val myDefaultHandshakeHandler: DefaultHandshakeHandler,
) : WebSocketMessageBrokerConfigurer {
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
            .addEndpoint("/stomp/platform") // 클라이언트가 연결할 엔드포인트
            .setAllowedOriginPatterns("*") // CORS 설정
            .setHandshakeHandler(myDefaultHandshakeHandler)
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry
            .enableSimpleBroker("/topic", "/queue") // 메시지 브로커 엔드포인트 prefix
            .setTaskScheduler(heartBeatScheduler)
            .setHeartbeatValue(longArrayOf(5000, 5000))
        registry.setApplicationDestinationPrefixes("/app") // 클라이언트에서 메시지 보낼 prefix
    }
}

@EnableAsync
@Configuration
class AsyncConfig {
    @Bean(name = ["taskExecutor"])
    @Primary
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 10
        executor.queueCapacity = 500
        executor.setThreadNamePrefix("MyExecutor-")
        executor.initialize()
        return executor
    }

    @Bean
    fun heartBeatScheduler(): TaskScheduler = ThreadPoolTaskScheduler()
}
