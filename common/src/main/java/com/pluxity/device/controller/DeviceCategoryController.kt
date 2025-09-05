package com.pluxity.device.controller

import com.pluxity.device.dto.DeviceCategoryDepthResponse
import com.pluxity.device.dto.DeviceCategoryRequest
import com.pluxity.device.dto.DeviceCategoryResponse
import com.pluxity.device.dto.DeviceCategoryUpdateRequest
import com.pluxity.device.dto.DeviceResponse
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
@RequestMapping("/device-categories")
@Tag(name = "Device Category Controller", description = "디바이스 카테고리 관리 API")
class DeviceCategoryController(
    private val deviceCategoryService: DeviceCategoryService,
) {
    @Operation(summary = "디바이스 카테고리 생성", description = "새로운 디바이스 카테고리를 생성합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "카테고리 생성 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
        ],
    )
    @PostMapping
    @ResponseCreated(path = "/device-categories/{id}")
    fun create(
        @Parameter(description = "카테고리 생성 정보", required = true)
        @Valid
        @RequestBody request: DeviceCategoryRequest,
    ): ResponseEntity<Long> {
        val id = deviceCategoryService.create(request)
        return ResponseEntity.ok(id)
    }

    @Operation(summary = "디바이스 카테고리 목록 조회", description = "모든 디바이스 카테고리 목록을 계층 구조로 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "목록 조회 성공"),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @GetMapping
    fun getAllCategories(): ResponseEntity<DataResponseBody<List<DeviceCategoryResponse>>> =
        ResponseEntity.ok(DataResponseBody(deviceCategoryService.getDeviceCategories()))

    @Operation(summary = "디바이스 카테고리 max depth 조회", description = "디바이스 카테고리 max depth를 조회합니다.")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "조회 성공")])
    @GetMapping("/max-depth")
    fun getCategoryDepth(): ResponseEntity<DataResponseBody<DeviceCategoryDepthResponse>> =
        ResponseEntity.ok(DataResponseBody(deviceCategoryService.getDeviceCategoryDepth()))

    @Operation(summary = "하위 디바이스 카테고리 목록 조회", description = "특정 카테고리의 직계 하위 카테고리 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "목록 조회 성공"),
            ApiResponse(
                responseCode = "404",
                description = "부모 카테고리를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @GetMapping("/{id}/children")
    fun getChildCategories(
        @Parameter(description = "부모 카테고리 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<DataResponseBody<List<DeviceCategoryResponse>>> =
        ResponseEntity.ok(DataResponseBody(deviceCategoryService.getChildDeviceCategories(id)))

    @Operation(summary = "카테고리 수정", description = "기존 카테고리의 정보를 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "카테고리 수정 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "카테고리를 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
        ],
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "카테고리 ID", required = true) @PathVariable id: Long,
        @Parameter(description = "카테고리 수정 정보", required = true)
        @Valid
        @RequestBody request: DeviceCategoryUpdateRequest,
    ): ResponseEntity<Void> {
        deviceCategoryService.update(id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "카테고리 삭제", description = "ID로 카테고리를 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "카테고리 삭제 성공"),
            ApiResponse(
                responseCode = "400",
                description = "카테고리에 디바이스가 등록되어 있어 삭제할 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "카테고리를 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
        ],
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "카테고리 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<Void> {
        deviceCategoryService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "카테고리에 속한 디바이스 조회",
        description = "카테고리ID로 카테고리에 속한 디바이스를 조회합니다",
        parameters = [
            Parameter(name = "facilityId", description = "시설 아이디", required = true, example = "1"),
        ],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "카테고리를 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "시설을 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
        ],
    )
    @GetMapping("/{id}/devices")
    fun getDevicesByCategoryId(
        @Parameter(description = "카테고리 ID") @PathVariable id: Long,
        @RequestParam("facilityId") facilityId: Long,
    ): ResponseEntity<DataResponseBody<List<DeviceResponse>>> =
        ResponseEntity.ok(
            DataResponseBody(deviceCategoryService.getDevicesByCategoryId(id, facilityId)),
        )
}
