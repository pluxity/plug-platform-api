package com.pluxity.onboarding

import com.pluxity.climate.ClimateData
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class OnboardingStep7DataCollectionTest {
    private val service =
        OnboardingCollector(
            clientFactory = mockk(relaxed = true),
        )

    @Test
    @DisplayName("병렬 처리 테스트 - 5개 동시 처리")
    fun getMockData_shouldProcessInParallel() =
        runBlocking {
            // given
            val callTimestamps = mutableListOf<Pair<Int, Long>>()

            // 코루틴의 각 호출의 시작 시간을 기록하여 병렬성 검증
            val mockFetcher: suspend (Int) -> ClimateData = { id ->
                val startTime = System.currentTimeMillis() // 시작 시간
                callTimestamps.add(id to startTime)
                println("[$id] 호출 시작: $startTime")
                delay(1000)
                ClimateData(
                    deviceId = "test-device-id",
                    temperature = 20.0,
                )
            }

            // when
            val startTime = System.currentTimeMillis()
            val result = service.getMockData(fetcher = mockFetcher)
            val endTime = System.currentTimeMillis()
            val totalTime = endTime - startTime

            // then
            assertEquals(5, result.size)
            println("총 소요 시간 : $totalTime")

            // 병렬이면 약 2초, 직렬이면 5초 이상
            assert(totalTime < 5000) {
                "직렬 처리 의심"
            }
        }

    @Test
    @DisplayName("재시도 테스트 - 2번 실패 후 성공")
    fun testRetry_shouldSuccessAfter_twoRetry() =
        runBlocking {
            // given
            var callCount = 0
            val mockFetcher: suspend (Int) -> ClimateData = { id ->
                callCount++
                println("Mock 호출 횟수: $callCount")

                when (callCount) {
                    1 -> throw RuntimeException("1번 실패")
                    2 -> throw RuntimeException("2번 실패")
                    else ->
                        ClimateData(
                            deviceId = "test-device-id",
                            temperature = 20.0,
                        )
                }
            }

            // when
            val result = service.fetchWithRetry(1, fetcher = mockFetcher)

            assertEquals(3, callCount)
            assertEquals("test-device-id", result.deviceId)
        }
}
