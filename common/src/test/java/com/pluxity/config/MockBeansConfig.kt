package com.pluxity.config

import com.pluxity.feature.service.FeatureAssignment
import com.pluxity.global.utils.SortUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Sort

@TestConfiguration
class MockBeansConfig {
    @Bean
    @Primary
    fun featureAssignment(): FeatureAssignment = mockk()

    @Bean
    @Primary
    fun mockSortUtils(): SortUtils {
        val mockSortUtils = mockk<SortUtils>()
        val mockSort =
            mockk<Sort>(relaxed = true) {
                every { isSorted } returns true
                every { isUnsorted } returns false
            }

        mockkStatic(SortUtils::class)
        every { SortUtils.getOrderByCreatedAtDesc() } returns mockSort

        return mockSortUtils
    }
}
