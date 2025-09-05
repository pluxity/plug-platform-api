package com.pluxity.asset.controller

import com.pluxity.asset.dto.AssetCategoryCreateRequest
import com.pluxity.asset.dto.AssetCategoryDepthResponse
import com.pluxity.asset.dto.AssetCategoryResponse
import com.pluxity.asset.dto.AssetCategoryUpdateRequest
import com.pluxity.asset.service.AssetCategoryService
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
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/asset-categories")
@RestController
@Tag(name = "Asset Category Controller", description = "에셋 카테고리 관리 API")
class AssetCategoryController(
    private val service: AssetCategoryService,
) {
    @Operation(summary = "에셋 카테고리 목록 조회", description = "모든 에셋 카테고리 목록을 조회합니다")
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
    fun getAllCategories(): ResponseEntity<DataResponseBody<List<AssetCategoryResponse>>> =
        ResponseEntity.ok(DataResponseBody(service.getAllCategories()))

    @Operation(summary = "에셋 카테고리 max depth 조회", description = "에셋 카테고리 max depth를 조회합니다")
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "조회 성공")])
    @GetMapping("/max-depth")
    fun getCategoryDepth(): ResponseEntity<DataResponseBody<AssetCategoryDepthResponse>> =
        ResponseEntity.ok(DataResponseBody(service.getCategoryDepth()))

    @Operation(summary = "하위 에셋 카테고리 목록 조회", description = "특정 카테고리의 하위 카테고리 목록을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "목록 조회 성공"),
            ApiResponse(
                responseCode = "404",
                description = "부모 카테고리를 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @GetMapping("/{id}/children")
    fun getChildCategories(
        @Parameter(description = "부모 카테고리 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<DataResponseBody<List<AssetCategoryResponse>>> = ResponseEntity.ok(DataResponseBody(service.getChildCategories(id)))

    @Operation(summary = "에셋 카테고리 생성", description = "새로운 에셋 카테고리를 생성합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "카테고리 생성 성공"),
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
    @ResponseCreated(path = "/asset-categories/{id}")
    fun createAssetCategory(
        @Parameter(description = "카테고리 생성 정보", required = true) @RequestBody @Valid request: AssetCategoryCreateRequest,
    ): ResponseEntity<Long> = ResponseEntity.ok(service.createAssetCategory(request))

    @Operation(summary = "에셋 카테고리 수정", description = "기존 에셋 카테고리의 정보를 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "카테고리 수정 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "카테고리를 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PutMapping("/{id}")
    fun updateAssetCategory(
        @Parameter(description = "카테고리 ID", required = true) @PathVariable id: Long,
        @Parameter(description = "카테고리 수정 정보", required = true) @RequestBody @Valid request: AssetCategoryUpdateRequest,
    ): ResponseEntity<Void> {
        service.updateAssetCategory(id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "에셋 카테고리 삭제", description = "ID로 에셋 카테고리를 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "카테고리 삭제 성공"),
            ApiResponse(
                responseCode = "400",
                description = "삭제할 수 없는 카테고리",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "카테고리를 찾을 수 없음",
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
    fun deleteAssetCategory(
        @Parameter(description = "카테고리 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<Void> {
        service.deleteAssetCategory(id)
        return ResponseEntity.noContent().build()
    }
}
