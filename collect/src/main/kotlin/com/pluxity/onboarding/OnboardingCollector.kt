package com.pluxity.onboarding

import com.pluxity.climate.ClimateData
import com.pluxity.config.WebClientFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.supervisorScope
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.awaitBody

/**
 * 온보딩 데이터 수집기
 *
 * Mock API로부터 데이터를 병렬로 수집, 재시도 로직 포함
 * - 5개의 디바이스 데이터를 동시에 수집
 * - 실패 시 최대 3회 재시도 (지수 백오프 적용)
 * - supervisorScope를 사용하여 일부 실패 시에도 나머지 처리 계속
 *
 */

@Component
class OnboardingCollector(
    clientFactory: WebClientFactory,
) {
    private val client = clientFactory.createClient("https://7f32047a-4f04-4221-bcd3-34e6a3534d85.mock.pstmn.io")

    suspend fun collectData(): List<ClimateData> {
        val mockData = getMockData()
        return mockData
    }

    /**
     * Mock 데이터를 병렬로 수집
     *
     * 5개의 디바이스(ID: 1~5)에 대해 동시에 HTTP 요청을 보내고,
     * 각 요청은 독립적으로 재시도 로직을 수행
     * 결과를 리스트로 받아야해서 async 사용
     *
     * @return 수집된 MockData 리스트 (최대 5개)
     *
     */
    suspend fun getMockData(fetcher: suspend (Int) -> ClimateData = { id -> fetchData(id) }): List<ClimateData> =
        supervisorScope {
            (1..5)
                .map { i ->
                    println("[$i] 시작 - ${Thread.currentThread().name}")
                    async { fetchWithRetry(id = i, fetcher = fetcher) } // 여기서 await() 하면 요청하고 바로 응답을 기다리기 떄문에 직렬처리됨
                }.awaitAll()
        }

    /**
     *   WebClient를 사용하여 비동기로 HTTP GET 요청을 수행
     */
    suspend fun fetchData(id: Int): ClimateData =
        client
            .get()
            .uri("?deviceId=$id")
            .retrieve()
            .awaitBody()

    /**
     *  재시도 로직 수행
     */
    suspend fun fetchWithRetry(
        id: Int,
        maxRetry: Int = 3,
        attempt: Int = 0,
        fetcher: suspend (Int) -> ClimateData,
    ): ClimateData =
        try {
            println("[$id] 시도 ${attempt + 1}/${maxRetry + 1}")
            fetcher(id)
        } catch (e: Exception) {
            if (attempt < maxRetry) {
                val delayTime = (1L shl attempt) * 1000L
                println("[$id] 실패, $delayTime ms 후 재시도")
                delay(delayTime)
                fetchWithRetry(id, maxRetry, attempt + 1, fetcher)
            } else {
                println("[$id] 모든 재시도 실패")
                throw e
            }
        }
}
