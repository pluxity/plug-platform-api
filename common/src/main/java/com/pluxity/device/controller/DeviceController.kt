package com.pluxity.device.controller

import com.pluxity.device.dto.DeviceInfoResponse
import com.pluxity.device.service.DeviceService
import com.pluxity.global.response.DataResponseBody
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/devices")
@Tag(name = "Device Controller", description = "디바이스 관리 API")
class DeviceController(
    private val deviceService: DeviceService,
) {
    @GetMapping
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "목록 조회 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                value = """
                            { "status": 200, "message": "성공", "data": [ { "id": "DAWONDNS-T....", "name": "온습도계", "type": "CLIMATE", "featureId": "ce9553fa-..." }
                        """,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    @Operation(summary = "디바이스 목록 조회", description = "모든 디바이스 목록을 조회합니다.")
    fun get(): ResponseEntity<DataResponseBody<List<DeviceInfoResponse>>> = ResponseEntity.ok(DataResponseBody.of(deviceService.findAll()))

    @GetMapping("/types")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "목록 조회 성공",
            ),
        ],
    )
    @Operation(summary = "디바이스 타입 목록 조회", description = "모든 디바이스 타입 목록을 조회합니다.")
    fun getAllType(): ResponseEntity<DataResponseBody<List<String>>> = ResponseEntity.ok(DataResponseBody.of(deviceService.findAllType()))
}
