package com.pluxity.collect.climate

import com.fasterxml.jackson.annotation.JsonProperty
import com.pluxity.collect.config.WebClientFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val log = KotlinLogging.logger {}

@Component
class ClimateDataCollector(
    private val climateDataRequest: ClimateDataRepository,
    webClientFactory: WebClientFactory,
) {
    private var tokenInfo: TokenInfo? = null
    private val tokenMutex = Mutex()

    private val client: WebClient =
        webClientFactory
            .createClient("https://dwcon.enercare.co.kr:18443")

    companion object {
        val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")
    }

    suspend fun collectClimateData(list: List<String>) {
        ensureToken()

        supervisorScope {
            list
                .map { id ->
                    launch {
                        runCatching {
                            val (deviceId, results) = callClimateDataWithRetry(id)
                            climateDataRequest.save(
                                ClimateData(
                                    deviceId = deviceId,
                                    temperature = results.temperature,
                                    humidity = results.humidity,
                                    status = results.connStatus,
                                    firmwareVersion = results.firmwareVersion,
                                    battery = results.battery,
                                    uploadTime = LocalDateTime.parse(results.uploadTime, FORMATTER),
                                ),
                            )
                        }.onFailure { e ->
                            log.warn(e) { "climate save failed for id=$id" }
                        }
                    }
                }
        }
    }

    private suspend fun callClimateDataWithRetry(
        deviceId: String,
        isRetry: Boolean = false,
    ): DeviceValuesResponse =
        try {
            callClimateData(deviceId)
        } catch (e: WebClientResponseException) {
            if (e.statusCode.value() == 400 && !isRetry && isTokenIncorrectError(e)) {
                log.warn { "토큰 무효로 인한 400 응답, 토큰 갱신 후 재시도: deviceId=$deviceId" }

                // 토큰 갱신을 동기화하여 중복 요청 방지
                tokenMutex.withLock {
                    // 400 에러가 발생했으므로 현재 토큰이 무효함 - 무조건 새로 발급
                    val oldTokenValue = tokenInfo?.value
                    log.info { "토큰 강제 갱신 시작 - 이전 토큰: ${oldTokenValue?.take(10)}..." }
                    ensureToken()
                    log.info { "토큰 강제 갱신 완료 - 새 토큰: ${tokenInfo?.value?.take(10)}..., 만료: ${tokenInfo?.expiresAt}" }
                }

                // 재시도 플래그를 true로 설정하여 재귀 호출
                callClimateDataWithRetry(deviceId, isRetry = true)
            } else {
                throw e
            }
        }

    private suspend fun ensureToken() {
        // 토큰이 없거나 만료 1시간 전인 경우에만 발급
        if (tokenInfo?.expiresAt?.isAfter(LocalDateTime.now().plusHours(1)) == true) return
        tokenInfo = fetchToken()
        log.info { "새 토큰 발급, 만료: ${tokenInfo?.expiresAt}" }
    }

    private fun isTokenIncorrectError(e: WebClientResponseException): Boolean =
        try {
            val responseBody = e.responseBodyAsString
            log.info { "400 응답 바디 확인: $responseBody" }
            responseBody.contains("\"reason\":\"Token Incorrect\"")
        } catch (ex: Exception) {
            log.warn { "응답 바디 파싱 중 오류 발생: ${ex.message}" }
            false
        }

    private suspend fun fetchToken(): TokenInfo {
        val (token, expire) =
            client
                .post()
                .uri("/conn/v1/publish/servertoken")
                .header("Authorization", "Basic UExVWElUWTI6WnpKWk9oWFVYMzBDV1M5WFQ5K3ZvbWw5aGIwcHgzV3UvdkZTaTAwblQ2dz0=")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(ServerTokenRequest("PLUXITY2"))
                .retrieve()
                .awaitBody<ServerTokenResponse>()

        return TokenInfo(
            value = token,
            expiresAt = LocalDateTime.parse(expire, FORMATTER),
        )
    }

    private suspend fun callClimateData(deviceId: String): DeviceValuesResponse =
        client
            .post()
            .uri("/conn/v1/inquire/device/values")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(
                DeviceValuesRequest(
                    dwdServerId = "PLUXITY2",
                    dwdAccessToken = requireNotNull(tokenInfo).value,
                    groupId = "PLUXITY",
                    deviceId = deviceId,
                    inquireValues =
                        listOf(
                            "conn_status",
                            "firmware_version",
                            "temperature",
                            "humidity",
                            "battery",
                            "upload_time",
                        ),
                ),
            ).retrieve()
            .awaitBody<DeviceValuesResponse>()

    data class TokenInfo(
        val value: String,
        val expiresAt: LocalDateTime,
    )

    data class ServerTokenRequest(
        @field:JsonProperty("dwd_group_id")
        val groupId: String,
    )

    data class ServerTokenResponse(
        @field:JsonProperty("dwd_access_token")
        val token: String,
        @field:JsonProperty("dwd_access_token_expiredate")
        val expire: String,
    )

    data class DeviceValuesRequest(
        @field:JsonProperty("dwd_server_id")
        val dwdServerId: String,
        @field:JsonProperty("dwd_access_token")
        val dwdAccessToken: String,
        @field:JsonProperty("group_id")
        val groupId: String,
        @field:JsonProperty("device_id")
        val deviceId: String,
        @field:JsonProperty("inquire_values")
        val inquireValues: List<String>,
    )

    data class DeviceValuesResponse(
        @field:JsonProperty("device_id")
        val deviceId: String,
        @field:JsonProperty("results")
        val results: ResultsData,
    )

    data class ResultsData(
        @field:JsonProperty("upload_time")
        val uploadTime: String,
        @field:JsonProperty("temperature")
        val temperature: Double,
        @field:JsonProperty("humidity")
        val humidity: Double,
        @field:JsonProperty("conn_status")
        val connStatus: Int,
        @field:JsonProperty("firmware_version")
        val firmwareVersion: String,
        @field:JsonProperty("battery")
        val battery: Double,
    )
}
