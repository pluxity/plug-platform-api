package com.pluxity.device.controller

import com.pluxity.device.dto.*
import com.pluxity.device.service.DeviceCategoryService
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
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/device-categories")
@RequiredArgsConstructor
@Tag(name = "Device Category Controller", description = "디바이스 카테고리 관리 API")
class DeviceCategoryController {
    private val deviceCategoryService: DeviceCategoryService? = null

    @Operation(summary = "디바이스 카테고리 생성", description = "새로운 디바이스 카테고리를 생성합니다")
    @ApiResponses(
        value = [ApiResponse(responseCode = "201", description = "카테고리 생성 성공"), ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(responseCode = "500", description = "서버 오류", content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class)))]
    )
    @PostMapping
    @ResponseCreated(path = "/device-categories/{id}")
    fun create(
        @Parameter(description = "카테고리 생성 정보", required = true) @RequestBody request: @Valid DeviceCategoryRequest
    ): ResponseEntity<Long?> {
        val id = deviceCategoryService!!.create(request)
        return ResponseEntity.ok<Long?>(id)
    }

    @get:GetMapping
    @get:ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "목록 조회 성공"
        ), ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = Content(schema = Schema(implementation = ErrorResponseBody::class))
        )]
    )
    @get:Operation(summary = "디바이스 카테고리 목록 조회", description = "모든 디바이스 카테고리 목록을 계층 구조로 조회합니다.")
    val allCategories: ResponseEntity<DataResponseBody<MutableList<DeviceCategoryResponse?>?>?>
        get() = ResponseEntity.ok<DataResponseBody<MutableList<DeviceCategoryResponse?>?>?>(
            DataResponseBody.of<MutableList<DeviceCategoryResponse?>?>(
                deviceCategoryService!!.getDeviceCategories()
            )
        )

    @get:GetMapping("/max-depth")
    @get:ApiResponses(value = [ApiResponse(responseCode = "200", description = "조회 성공")])
    @get:Operation(summary = "디바이스 카테고리 max depth 조회", description = "디바이스 카테고리 max depth를 조회합니다.")
    val categoryDepth: ResponseEntity<DataResponseBody<DeviceCategoryDepthResponse?>?>
        get() = ResponseEntity.ok<DataResponseBody<DeviceCategoryDepthResponse?>?>(DataResponseBody.of<DeviceCategoryDepthResponse?>(deviceCategoryService!!.getDeviceCategoryDepth()))

    @Operation(summary = "하위 디바이스 카테고리 목록 조회", description = "특정 카테고리의 직계 하위 카테고리 목록을 조회합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "200", description = "목록 조회 성공"), ApiResponse(
            responseCode = "404",
            description = "부모 카테고리를 찾을 수 없음",
            content = Content(schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(responseCode = "500", description = "서버 오류", content = Content(schema = Schema(implementation = ErrorResponseBody::class)))]
    )
    @GetMapping("/{id}/children")
    fun getChildCategories(
        @Parameter(description = "부모 카테고리 ID", required = true) @PathVariable id: Long
    ): ResponseEntity<DataResponseBody<MutableList<DeviceCategoryResponse?>?>?> {
        return ResponseEntity.ok<DataResponseBody<MutableList<DeviceCategoryResponse?>?>?>(
            DataResponseBody.of<MutableList<DeviceCategoryResponse?>?>(deviceCategoryService!!.getChildDeviceCategories(id))
        )
    }

    @Operation(summary = "카테고리 수정", description = "기존 카테고리의 정보를 수정합니다")
    @ApiResponses(
        value = [ApiResponse(responseCode = "204", description = "카테고리 수정 성공"), ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(
            responseCode = "404",
            description = "카테고리를 찾을 수 없음",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(responseCode = "500", description = "서버 오류", content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class)))]
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "카테고리 ID", required = true) @PathVariable id: Long,
        @Parameter(description = "카테고리 수정 정보", required = true) @RequestBody request: @Valid DeviceCategoryUpdateRequest
    ): ResponseEntity<Void?> {
        deviceCategoryService!!.update(id, request)
        return ResponseEntity.noContent().build<Void?>()
    }

    @Operation(summary = "카테고리 삭제", description = "ID로 카테고리를 삭제합니다")
    @ApiResponses(
        value = [ApiResponse(responseCode = "204", description = "카테고리 삭제 성공"), ApiResponse(
            responseCode = "400",
            description = "카테고리에 디바이스가 등록되어 있어 삭제할 수 없음",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(
            responseCode = "404",
            description = "카테고리를 찾을 수 없음",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(responseCode = "500", description = "서버 오류", content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class)))]
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "카테고리 ID", required = true) @PathVariable id: Long
    ): ResponseEntity<Void?> {
        deviceCategoryService!!.delete(id)
        return ResponseEntity.noContent().build<Void?>()
    }

    @Operation(
        summary = "카테고리에 속한 디바이스 조회",
        description = "카테고리ID로 카테고리에 속한 디바이스를 조회합니다",
        parameters = [Parameter(name = "facilityId", description = "시설 아이디", required = true, example = "1")]
    )
    @ApiResponses(
        value = [ApiResponse(responseCode = "200", description = "조회 성공", content = Content(mediaType = "application/json")), ApiResponse(
            responseCode = "404",
            description = "카테고리를 찾을 수 없음",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(
            responseCode = "404",
            description = "시설을 찾을 수 없음",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(responseCode = "500", description = "서버 오류", content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class)))]
    )
    @GetMapping("/{id}/devices")
    fun getDevicesByCategoryId(
        @Parameter(description = "카테고리 ID") @PathVariable id: Long,
        @RequestParam("facilityId") facilityId: Long
    ): ResponseEntity<DataResponseBody<MutableList<DeviceResponse?>?>?> {
        return ResponseEntity.ok<DataResponseBody<MutableList<DeviceResponse?>?>?>(
            DataResponseBody.of<MutableList<DeviceResponse?>?>(deviceCategoryService!!.getDevicesByCategoryId(id, facilityId))
        )
    }
}
