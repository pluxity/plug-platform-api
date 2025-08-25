package com.pluxity.collect.climate

import com.pluxity.collect.climate.dto.ClimateDataResponse
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
@RequestMapping("/devices/climates")
@Tag(name = "Climate data Controller", description = "온습도계 데이터 조회 API")
class ClimateDataController(
    private val climateDataService: ClimateDataService,
) {
    @Operation(summary = "온습도계 데이터 조회", description = "ID로 특정 온습도계의 데이터 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "온습도계 데이터 조회 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "해당 ID의 온습도계를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @GetMapping("/{id}/values")
    fun getById(
        @Parameter(description = "온습도계 ID", required = true) @PathVariable id: String,
        @Parameter(description = "조회 시작일(yyyyMMddHHmmss)", required = true)
        @RequestParam("startTime") startTime: String,
        @Parameter(description = "조회 종료일(yyyyMMddHHmmss)", required = true)
        @RequestParam("endTime") endTime: String,
    ): ResponseEntity<DataResponseBody<List<ClimateDataResponse>>> =
        ResponseEntity.ok(DataResponseBody.of(climateDataService.findData(id, startTime, endTime)))
}
