package com.pluxity.config

import com.pluxity.feature.service.FeatureAssignType
import com.pluxity.feature.service.FeatureAssignment
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class MockBeansConfig {
    @Bean(name = ["cctvService"])
    fun cctvService(): FeatureAssignment =
        mockk {
            every { type } returns FeatureAssignType.CCTV
        }

    @Bean(name = ["temperatureHumidityService"])
    fun temperatureHumidityService(): FeatureAssignment =
        mockk {
            every { type } returns FeatureAssignType.THERMO_HYGROMETER
        }
}
