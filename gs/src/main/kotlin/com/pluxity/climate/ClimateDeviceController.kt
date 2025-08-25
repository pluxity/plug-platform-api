package com.pluxity.climate

import com.pluxity.climate.dto.ClimateDeviceCreateRequest
import com.pluxity.climate.dto.ClimateDeviceResponse
import com.pluxity.climate.dto.ClimateDeviceUpdateRequest
import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.global.response.DataResponseBody
import com.pluxity.global.response.ErrorResponseBody
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
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/devices/climates")
@Tag(name = "Climate Controller", description = "온습도계 관리 API")
class ClimateDeviceController(
    private val climateDeviceService: ClimateDeviceService,
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
    @ResponseCreated(path = "/climates/{id}")
    fun create(
        @Parameter(description = "온습도계 생성 정보", required = true) @RequestBody request: @Valid ClimateDeviceCreateRequest,
    ): ResponseEntity<String> = ResponseEntity.ok(climateDeviceService.save(request))

    @GetMapping
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "목록 조회 성공",
            ),
        ],
    )
    @Operation(summary = "온습도계 목록 조회", description = "모든 온습도계 목록을 조회합니다.")
    fun get(): ResponseEntity<DataResponseBody<List<ClimateDeviceResponse>>> =
        ResponseEntity.ok(DataResponseBody.of(climateDeviceService.findAll()))

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
    ): ResponseEntity<DataResponseBody<ClimateDeviceResponse>> = ResponseEntity.ok(DataResponseBody.of(climateDeviceService.findById(id)))

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
        @Parameter(description = "온습도계 수정 정보", required = true) @RequestBody request: @Valid ClimateDeviceUpdateRequest,
    ): ResponseEntity<Void> {
        climateDeviceService.putUpdate(id, request)
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
        climateDeviceService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "온습도계에 카테고리 할당", description = "특정 온습도계에 카테고리를 할당(연결)합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "카테고리 할당 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 온습도계 또는 카테고리를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PatchMapping("/{deviceId}/category/{categoryId}")
    fun assignCategory(
        @Parameter(description = "온습도계 ID", required = true) @PathVariable deviceId: String,
        @Parameter(description = "할당할 카테고리 ID", required = true) @PathVariable categoryId: Long,
    ): ResponseEntity<Void> {
        climateDeviceService.assignCategory(deviceId, categoryId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "온습도계의 카테고리 제거", description = "특정 온습도계에 할당된 카테고리를 제거(연결 해제)합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "카테고리 제거 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 온습도계를 찾을 수 없거나, 온습도계에 할당된 카테고리가 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @DeleteMapping("/{deviceId}/category")
    fun removeCategory(
        @Parameter(description = "온습도계 ID", required = true) @PathVariable deviceId: String,
    ): ResponseEntity<Void> {
        climateDeviceService.removeCategory(deviceId)
        return ResponseEntity.noContent().build()
    }
}
