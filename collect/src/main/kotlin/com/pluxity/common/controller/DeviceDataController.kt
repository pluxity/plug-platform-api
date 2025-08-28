package com.pluxity.common.controller

import com.pluxity.climate.ClimateDataService
import com.pluxity.climate.dto.ClimateDataResponse
import com.pluxity.common.dto.DeviceDataResponse
import com.pluxity.common.enum.DataInterval
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceCompanyType.DAWONDNS
import com.pluxity.device.entity.DeviceType
import com.pluxity.device.entity.DeviceType.TEMP_HUM
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
@RequestMapping("/devices")
@Tag(name = "Device Data Controller", description = "데이터 조회 API")
class DeviceDataController(
    private val climateDataService: ClimateDataService,
) {
    //    @Operation(summary = "온습도계 시간별 데이터 조회", description = "ID로 특정 온습도계의 데이터 정보를 시간별로 조회합니다.")
//    @ApiResponses(
//        value = [
//            ApiResponse(
//                responseCode = "200",
//                description = "온습도계 시간별 데이터 조회 성공",
//            ),
//            ApiResponse(
//                responseCode = "404",
//                description = "해당 ID의 온습도계를 찾을 수 없음",
//                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
//            ),
//        ],
//    )

//    @GetMapping("/{companyType}/{deviceType}/{id}/values")
    fun getPeriodData(
        @Parameter(description = "디바이스 회사", required = true) @PathVariable companyType: DeviceCompanyType,
        @Parameter(description = "디바이스 타입", required = true) @PathVariable deviceType: DeviceType,
        @Parameter(description = "온습도계 ID", required = true) @PathVariable id: String,
        @Parameter(description = "데이터 집계 간격", example = "HOURLY")
        @RequestParam(defaultValue = "HOURLY", required = false) interval: DataInterval,
        @Parameter(description = "조회 시작일(yyyyMMddHHmmss)", required = true)
        @RequestParam("startTime") startTime: String,
        @Parameter(description = "조회 종료일(yyyyMMddHHmmss)", required = true)
        @RequestParam("endTime") endTime: String,
    ): ResponseEntity<DataResponseBody<List<ClimateDataResponse>>> {
        val result =
            when (companyType) {
                DAWONDNS ->
                    when (deviceType) {
                        TEMP_HUM -> {
                            climateDataService.getPeriodData(id, interval, startTime, endTime)
                        }
                    }
            }
        return ResponseEntity.ok(DataResponseBody.of(result))
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
    @GetMapping("/{companyType}/{deviceType}/{id}/latest")
    fun getLatestData(
        @Parameter(description = "디바이스 회사", required = true) @PathVariable companyType: DeviceCompanyType,
        @Parameter(description = "디바이스 타입", required = true) @PathVariable deviceType: DeviceType,
        @Parameter(description = "온습도계 ID", required = true) @PathVariable id: String,
    ): ResponseEntity<DataResponseBody<DeviceDataResponse>> {
        val result =
            when (companyType) {
                DAWONDNS ->
                    when (deviceType) {
                        TEMP_HUM -> {
                            climateDataService.getLatestData(id)
                        }
                    }
            }
        return ResponseEntity.ok(DataResponseBody.of(result))
    }
}
