package com.pluxity.temperaturehumidity.controller

import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.global.response.DataResponseBody
import com.pluxity.global.response.ErrorResponseBody
import com.pluxity.temperaturehumidity.dto.TemperatureHumidityCreateRequest
import com.pluxity.temperaturehumidity.dto.TemperatureHumidityResponse
import com.pluxity.temperaturehumidity.dto.TemperatureHumidityUpdateRequest
import com.pluxity.temperaturehumidity.service.TemperatureHumidityService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/temperature-humidity-devices")
@Tag(name = "Temperature Humidity Controller", description = "온습도계 관리 API")
class TemperatureHumidityController(
    private val temperatureHumidityService: TemperatureHumidityService,
) {
    @Operation(summary = "온습도계 생성", description = "새로운 온습도계를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "온습도계 생성 성공",
            ), ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PostMapping
    @ResponseCreated(path = "/temperature-humidity-devices/{id}")
    fun create(
        @Parameter(description = "온습도계 생성 정보", required = true) @RequestBody request: @Valid TemperatureHumidityCreateRequest,
    ): ResponseEntity<String> = ResponseEntity.ok(temperatureHumidityService.save(request))

    @Operation(summary = "온습도계 목록 조회", description = "모든 온습도계 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "목록 조회 성공",
            ),
        ],
    )
    @GetMapping
    fun get(
        @Parameter(description = "시설 아이디") @RequestParam("facilityId", required = false) facilityId: Long?,
    ): ResponseEntity<DataResponseBody<List<TemperatureHumidityResponse>>> =
        ResponseEntity.ok(DataResponseBody(temperatureHumidityService.findAll(facilityId)))

    @Operation(summary = "온습도계 상세 조회", description = "ID로 특정 온습도계의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "온습도계 조회 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 온습도계를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @GetMapping("/{id}")
    fun getById(
        @Parameter(description = "온습도계 ID", required = true) @PathVariable id: String,
    ): ResponseEntity<DataResponseBody<TemperatureHumidityResponse>> =
        ResponseEntity.ok(DataResponseBody(temperatureHumidityService.findById(id)))

    @Operation(summary = "온습도계 정보 수정", description = "ID로 특정 온습도계의 정보를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "온습도계 수정 성공",
            ), ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 온습도계를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "온습도계 ID", required = true) @PathVariable id: String,
        @Parameter(description = "온습도계 수정 정보", required = true) @RequestBody request: @Valid TemperatureHumidityUpdateRequest,
    ): ResponseEntity<Void> {
        temperatureHumidityService.putUpdate(id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "온습도계 삭제", description = "ID로 특정 온습도계를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "온습도계 삭제 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 온습도계를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "온습도계 ID", required = true) @PathVariable id: String,
    ): ResponseEntity<Void> {
        temperatureHumidityService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
