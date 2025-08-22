package com.pluxity.config

import com.pluxity.feature.service.FeatureAssignment
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class MockBeansConfig {
    @Bean
    @Primary
    fun featureAssignment(): FeatureAssignment = mockk()
}
