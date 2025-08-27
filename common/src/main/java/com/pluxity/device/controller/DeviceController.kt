package com.pluxity.device.controller

import com.pluxity.device.dto.DeviceCreateRequest
import com.pluxity.device.dto.DeviceResponse
import com.pluxity.device.dto.DeviceUpdateRequest
import com.pluxity.device.dto.TypeKeyValueResponse
import com.pluxity.device.service.DeviceService
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
@RequestMapping("/devices")
@Tag(name = "Device Controller", description = "디바이스 관리 API")
class DeviceController(
    private val deviceService: DeviceService,
) {
    @Operation(summary = "디바이스 생성", description = "새로운 디바이스를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "디바이스 생성 성공",
            ), ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PostMapping
    @ResponseCreated(path = "/devices/{id}")
    fun create(
        @Parameter(description = "디바이스 생성 정보", required = true) @RequestBody request: @Valid DeviceCreateRequest,
    ): ResponseEntity<String> = ResponseEntity.ok(deviceService.save(request))

    @Operation(summary = "디바이스 목록 조회", description = "모든 디바이스 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "목록 조회 성공",
            ),
        ],
    )
    @GetMapping
    fun get(): ResponseEntity<DataResponseBody<List<DeviceResponse>>> = ResponseEntity.ok(DataResponseBody.of(deviceService.findAll()))

    @Operation(summary = "디바이스 타입 목록 조회", description = "모든 디바이스 타입 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "목록 조회 성공",
            ),
        ],
    )
    @GetMapping("/types")
    fun getAllType(): ResponseEntity<DataResponseBody<List<TypeKeyValueResponse>>> =
        ResponseEntity.ok(DataResponseBody.of(deviceService.findAllType()))

    @Operation(summary = "디바이스 회사 목록 조회", description = "모든 디바이스 회사 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "목록 조회 성공",
            ),
        ],
    )
    @GetMapping("/company-types")
    fun getAllCompanyType(): ResponseEntity<DataResponseBody<List<TypeKeyValueResponse>>> =
        ResponseEntity.ok(DataResponseBody.of(deviceService.findAllCompanyType()))

    @Operation(summary = "디바이스 상세 조회", description = "ID로 특정 디바이스의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "디바이스 조회 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 디바이스를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @GetMapping("/{id}")
    fun getById(
        @Parameter(description = "디바이스 ID", required = true) @PathVariable id: String,
    ): ResponseEntity<DataResponseBody<DeviceResponse>> = ResponseEntity.ok(DataResponseBody.of(deviceService.findById(id)))

    @Operation(summary = "디바이스 정보 수정", description = "ID로 특정 디바이스의 정보를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "디바이스 수정 성공",
            ), ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 디바이스를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "디바이스 ID", required = true) @PathVariable id: String,
        @Parameter(description = "디바이스 수정 정보", required = true) @RequestBody request: @Valid DeviceUpdateRequest,
    ): ResponseEntity<Void> {
        deviceService.putUpdate(id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "디바이스 삭제", description = "ID로 특정 디바이스를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "디바이스 삭제 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 디바이스를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "디바이스 ID", required = true) @PathVariable id: String,
    ): ResponseEntity<Void> {
        deviceService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "디바이스에 카테고리 할당", description = "특정 디바이스에 카테고리를 할당(연결)합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "카테고리 할당 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 디바이스 또는 카테고리를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PatchMapping("/{deviceId}/category/{categoryId}")
    fun assignCategory(
        @Parameter(description = "디바이스 ID", required = true) @PathVariable deviceId: String,
        @Parameter(description = "할당할 카테고리 ID", required = true) @PathVariable categoryId: Long,
    ): ResponseEntity<Void> {
        deviceService.assignCategory(deviceId, categoryId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "디바이스의 카테고리 제거", description = "특정 디바이스에 할당된 카테고리를 제거(연결 해제)합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "카테고리 제거 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 디바이스를 찾을 수 없거나, 디바이스에 할당된 카테고리가 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @DeleteMapping("/{deviceId}/category")
    fun removeCategory(
        @Parameter(description = "디바이스 ID", required = true) @PathVariable deviceId: String,
    ): ResponseEntity<Void> {
        deviceService.removeCategory(deviceId)
        return ResponseEntity.noContent().build()
    }
}
