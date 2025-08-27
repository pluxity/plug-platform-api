package com.pluxity.climate

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val log = KotlinLogging.logger {}

@Component
class ClimateDataCollector(
    private val climateDataRequest: ClimateDataRepository,
) {
    var tokenInfo: TokenInfo? = null

    private val client: WebClient =
        WebClient
            .builder()
            .baseUrl("https://dwcon.enercare.co.kr:18443")
            .defaultHeaders { it.accept = listOf(MediaType.APPLICATION_JSON) }
            .build()

    companion object {
        val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")
    }

    suspend fun collectClimateData(list: List<String>) {
        ensureToken()

        coroutineScope {
            list
                .map { id ->
                    async {
                        val (deviceId, results) = callClimateData(id)
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
                    }
                }.awaitAll()
        }
    }

    private suspend fun ensureToken() {
        if (tokenInfo?.expiresAt?.isAfter(LocalDateTime.now().plusHours(1)) == true) return
        tokenInfo = fetchToken()
        log.info { "새 토큰 발급, 만료: ${tokenInfo?.expiresAt}" }
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
