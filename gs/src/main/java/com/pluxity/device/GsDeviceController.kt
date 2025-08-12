package com.pluxity.device

import com.pluxity.cctv.dto.CctvResponse
import com.pluxity.device.dto.GsDeviceCctvUpdateRequest
import com.pluxity.device.dto.GsDeviceCreateRequest
import com.pluxity.device.dto.GsDeviceResponse
import com.pluxity.device.dto.GsDeviceUpdateRequest
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
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/devices")
@Tag(name = "Device Controller", description = "디바이스 관리 API")
class GsDeviceController(
    val gsDeviceService: GsDeviceService
) {
    @Operation(summary = "디바이스 생성", description = "새로운 디바이스를 생성합니다.")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "201",
            description = "디바이스 생성 성공"
        ), ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [Content(schema = Schema(implementation = ErrorResponseBody::class))]
        )]
    )
    @PostMapping
    @ResponseCreated(path = "/devices/{id}")
    fun create(
        @Parameter(description = "디바이스 생성 정보", required = true) @RequestBody request: @Valid GsDeviceCreateRequest
    ): ResponseEntity<String> {
        val id = gsDeviceService.save(request)
        return ResponseEntity.ok(id)
    }

    @GetMapping
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "목록 조회 성공"
        )]
    )
    @Operation(summary = "디바이스 목록 조회", description = "모든 디바이스 목록을 조회합니다.")
    fun get(): ResponseEntity<DataResponseBody<List<GsDeviceResponse>>> =
        ResponseEntity.ok(DataResponseBody.of(gsDeviceService.findAll()))


    @Operation(summary = "디바이스 상세 조회", description = "ID로 특정 디바이스의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "디바이스 조회 성공"
        ), ApiResponse(
            responseCode = "404",
            description = "해당 ID의 디바이스를 찾을 수 없음",
            content = [Content(schema = Schema(implementation = ErrorResponseBody::class))]
        )]
    )
    @GetMapping("/{id}")
    fun getById(
        @Parameter(description = "디바이스 ID", required = true) @PathVariable id: String
    ): ResponseEntity<DataResponseBody<GsDeviceResponse?>?> {
        return ResponseEntity.ok<DataResponseBody<GsDeviceResponse?>?>(
            DataResponseBody.of<GsDeviceResponse?>(
                gsDeviceService.findById(id)
            )
        )
    }

    @Operation(summary = "디바이스 정보 수정", description = "ID로 특정 디바이스의 정보를 수정합니다.")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "204",
            description = "디바이스 수정 성공"
        ), ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [Content(schema = Schema(implementation = ErrorResponseBody::class))]
        ), ApiResponse(
            responseCode = "404",
            description = "해당 ID의 디바이스를 찾을 수 없음",
            content = [Content(schema = Schema(implementation = ErrorResponseBody::class))]
        )]
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "디바이스 ID", required = true) @PathVariable id: String,
        @Parameter(description = "디바이스 수정 정보", required = true) @RequestBody request: @Valid GsDeviceUpdateRequest
    ): ResponseEntity<Void> {
        gsDeviceService.putUpdate(id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "디바이스 삭제", description = "ID로 특정 디바이스를 삭제합니다.")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "204",
            description = "디바이스 삭제 성공"
        ), ApiResponse(
            responseCode = "404",
            description = "해당 ID의 디바이스를 찾을 수 없음",
            content = [Content(schema = Schema(implementation = ErrorResponseBody::class))]
        )]
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "`디바이스 ID", required = true) @PathVariable id: String
    ): ResponseEntity<Void> {
        gsDeviceService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "디바이스에 카테고리 할당", description = "특정 디바이스에 카테고리를 할당(연결)합니다.")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "204",
            description = "카테고리 할당 성공"
        ), ApiResponse(
            responseCode = "404",
            description = "해당 ID의 디바이스 또는 카테고리를 찾을 수 없음",
            content = [Content(schema = Schema(implementation = ErrorResponseBody::class))]
        )]
    )
    @PatchMapping("/{deviceId}/category/{categoryId}")
    fun assignCategory(
        @Parameter(description = "디바이스 ID", required = true) @PathVariable deviceId: String,
        @Parameter(description = "할당할 카테고리 ID", required = true) @PathVariable categoryId: Long
    ): ResponseEntity<Void> {
        gsDeviceService.assignCategory(deviceId, categoryId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "디바이스의 카테고리 제거", description = "특정 디바이스에 할당된 카테고리를 제거(연결 해제)합니다.")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "204",
            description = "카테고리 제거 성공"
        ), ApiResponse(
            responseCode = "404",
            description = "해당 ID의 디바이스를 찾을 수 없거나, 디바이스에 할당된 카테고리가 없음",
            content = [Content(schema = Schema(implementation = ErrorResponseBody::class))]
        )]
    )
    @DeleteMapping("/{deviceId}/category")
    fun removeCategory(
        @Parameter(description = "디바이스 ID", required = true) @PathVariable deviceId: String
    ): ResponseEntity<Void> {
        gsDeviceService.removeCategory(deviceId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "디바이스에 연결된 CCTV 조회", description = "디바이스에 연결된 CCTV 정보를 조회합니다.")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "CCTV 정보 조회 성공"
        ), ApiResponse(
            responseCode = "404",
            description = "해당 ID의 디바이스를 찾을 수 없음",
            content = [Content(schema = Schema(implementation = ErrorResponseBody::class))]
        )]
    )

    @GetMapping("/{deviceId}/cctvs")
    fun getCctvByDeviceId(
        @Parameter(description = "디바이스 ID", required = true) @PathVariable deviceId: String
    ): ResponseEntity<DataResponseBody<List<CctvResponse>>> =
        ResponseEntity.ok(DataResponseBody.of(gsDeviceService.getCctvByDeviceId(deviceId)))


    @Operation(summary = "디바이스에 CCTV 설정", description = "특정 디바이스에 CCTV를 연결 및 해제 합니다.")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "204",
            description = "CCTV 설정 성공"
        ), ApiResponse(responseCode = "404", description = "디바이스 또는 CCTV를 찾을 수 없음")]
    )
    @PutMapping("/{deviceId}/cctvs")
    fun assignCctvToDevice(
        @Parameter(description = "디바이스 ID", required = true) @PathVariable deviceId: String,
        @Parameter(description = "설정 CCTV 정보", required = true) @RequestBody request: GsDeviceCctvUpdateRequest
    ): ResponseEntity<Void> {
        gsDeviceService.assignCctvToDevice(deviceId, request)
        return ResponseEntity.noContent().build()
    }
}
