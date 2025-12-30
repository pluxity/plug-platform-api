package com.pluxity.collect.common.controller

import com.pluxity.collect.climate.ClimateDataService
import com.pluxity.collect.common.dto.DeviceDataResponse
import com.pluxity.collect.common.dto.DeviceListDataResponse
import com.pluxity.collect.common.enum.DataInterval
import com.pluxity.global.response.DataResponseBody
import com.pluxity.global.response.ErrorResponseBody
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/temperature-humidity-devices")
@Tag(name = "Temperature Humidity Data Controller", description = "온습도계 데이터 조회 API")
class TemperatureHumidityDataController(
    private val climateDataService: ClimateDataService,
) {
    @Operation(summary = "온습도계 시간별 데이터 조회", description = "ID로 특정 온습도계의 데이터 정보를 시간별로 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "온습도계 시간별 데이터 조회 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "해당 ID의 온습도계를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @GetMapping("/{id}/time-series")
    fun getPeriodData(
        @Parameter(description = "온습도계 ID", required = true) @PathVariable id: String,
        @Parameter(description = "데이터 집계 간격", example = "HOUR")
        @RequestParam(defaultValue = "HOUR", required = false) interval: DataInterval,
        @Parameter(description = "조회 시작일(yyyyMMddHHmmss)", required = true)
        @RequestParam("from") from: String,
        @Parameter(description = "조회 종료일(yyyyMMddHHmmss)", required = true)
        @RequestParam("to") to: String,
    ): ResponseEntity<DataResponseBody<DeviceListDataResponse>> {
        val result = climateDataService.getTimeSeries(id, interval, from, to)
        return ResponseEntity.ok(DataResponseBody(result))
    }

    @Operation(summary = "온습도계 최근 데이터 조회", description = "ID로 특정 온습도계의 최근 데이터 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "온습도계 최근 데이터 조회 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "해당 ID의 온습도계를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "최근 데이터가 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @GetMapping("/{id}/latest")
    fun getLatestData(
        @Parameter(description = "온습도계 ID", required = true) @PathVariable id: String,
    ): ResponseEntity<DataResponseBody<DeviceDataResponse>> {
        val result = climateDataService.getLatestData(id)
        return ResponseEntity.ok(DataResponseBody(result))
    }
}
