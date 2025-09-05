package com.pluxity.asset.controller

import com.pluxity.asset.dto.AssetCreateRequest
import com.pluxity.asset.dto.AssetResponse
import com.pluxity.asset.dto.AssetUpdateRequest
import com.pluxity.asset.service.AssetService
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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/assets")
@RestController
@Tag(name = "Asset Controller", description = "에셋 관리 API")
class AssetController(
    private val service: AssetService,
) {
    @Operation(summary = "에셋 목록 조회", description = "모든 에셋 목록을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "목록 조회 성공"),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @GetMapping
    fun getAssets(): ResponseEntity<DataResponseBody<List<AssetResponse>>> = ResponseEntity.ok(DataResponseBody(service.getAssets()))

    @Operation(summary = "에셋 상세 조회", description = "ID로 특정 에셋의 상세 정보를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "에셋 조회 성공"),
            ApiResponse(
                responseCode = "404",
                description = "에셋을 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @GetMapping("/{id}")
    fun getAsset(
        @Parameter(description = "에셋 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<DataResponseBody<AssetResponse>> = ResponseEntity.ok(DataResponseBody(service.getAsset(id)))

    @Operation(summary = "에셋 생성", description = "새로운 에셋을 생성합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "에셋 생성 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PostMapping
    @ResponseCreated(path = "/assets/{id}")
    fun createAsset(
        @Parameter(description = "에셋 생성 정보", required = true) @RequestBody @Valid request: AssetCreateRequest,
    ): ResponseEntity<Long> = ResponseEntity.ok(service.createAsset(request))

    @Operation(summary = "에셋 수정", description = "기존 에셋의 정보를 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "에셋 수정 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "에셋을 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PatchMapping("/{id}")
    fun updateAsset(
        @Parameter(description = "에셋 ID", required = true) @PathVariable id: Long,
        @Parameter(description = "에셋 수정 정보", required = true) @RequestBody @Valid request: AssetUpdateRequest,
    ): ResponseEntity<Void> {
        service.updateAsset(id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "에셋 삭제", description = "ID로 에셋을 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "에셋 삭제 성공"),
            ApiResponse(
                responseCode = "404",
                description = "에셋을 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @DeleteMapping("/{id}")
    fun deleteAsset(
        @Parameter(description = "에셋 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<Void> {
        service.deleteAsset(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "에셋에 카테고리 할당", description = "에셋에 특정 카테고리를 할당합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "카테고리 할당 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "에셋 또는 카테고리를 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PatchMapping("/{id}/category/{categoryId}")
    fun assignCategory(
        @Parameter(description = "에셋 ID", required = true) @PathVariable id: Long,
        @Parameter(description = "카테고리 ID", required = true) @PathVariable categoryId: Long,
    ): ResponseEntity<Void> {
        service.assignCategory(id, categoryId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "에셋에서 카테고리 제거", description = "에셋에서 카테고리 할당을 제거합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "카테고리 제거 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "에셋을 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @DeleteMapping("/{id}/category")
    fun removeCategory(
        @Parameter(description = "에셋 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<Void> {
        service.removeCategory(id)
        return ResponseEntity.noContent().build()
    }
}
