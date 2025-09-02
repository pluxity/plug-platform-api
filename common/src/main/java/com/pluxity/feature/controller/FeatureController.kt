package com.pluxity.feature.controller

import com.pluxity.feature.dto.FeatureAssignDto
import com.pluxity.feature.dto.FeatureCreateRequest
import com.pluxity.feature.dto.FeatureResponse
import com.pluxity.feature.dto.FeatureUpdateRequest
import com.pluxity.feature.service.FeatureService
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
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/features")
@Tag(name = "Feature Controller", description = "피처 관리 API")
class FeatureController(
    private val featureService: FeatureService,
) {
    @Operation(summary = "피처 생성", description = "새로운 피처를 생성합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "피처 생성 성공",
            ), ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ), ApiResponse(
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
    fun createFeature(
        @Parameter(description = "피처 생성 정보", required = true) @RequestBody @Valid request: FeatureCreateRequest,
    ): ResponseEntity<FeatureResponse> = ResponseEntity.status(HttpStatus.CREATED).body(featureService.createFeature(request))

    @Operation(
        summary = "피처 목록 조회",
        description = "시설에 해당하는 모든 피처 목록을 조회합니다",
        parameters = [Parameter(name = "facilityId", description = "시설 아이디", required = true, example = "1")],
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "목록 조회 성공",
            ), ApiResponse(
                responseCode = "400",
                description = "파라미터 오류",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ), ApiResponse(
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
    @GetMapping
    fun getFeatures(
        @RequestParam("facilityId") facilityId: Long,
    ): ResponseEntity<DataResponseBody<List<FeatureResponse>>> =
        ResponseEntity.ok(DataResponseBody.of(featureService.getFeatures(facilityId)))

    @Operation(summary = "피처 정보 수정", description = "ID로 피처 정보를 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "피처 수정 성공",
            ), ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ), ApiResponse(
                responseCode = "404",
                description = "피처를 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ), ApiResponse(
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
    @PatchMapping("/{id}/transform")
    fun updateFeature(
        @Parameter(description = "피처 ID", required = true) @PathVariable id: String,
        @Parameter(description = "피처 수정 정보", required = true) @RequestBody @Valid request: FeatureUpdateRequest,
    ): ResponseEntity<Unit> {
        featureService.updateFeature(id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "피처 삭제", description = "ID로 피처를 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "피처 삭제 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "피처를 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ), ApiResponse(
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
    fun deleteFeature(
        @Parameter(description = "피처 ID", required = true) @PathVariable id: String,
    ): ResponseEntity<Unit> {
        featureService.deleteFeature(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "피처 연결", description = "특정 피처에 연결합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "연결 성공",
                content = [Content(schema = Schema(implementation = FeatureResponse::class))],
            ), ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (예: 피처가 이미 다른 곳에 할당됨)",
            ), ApiResponse(responseCode = "404", description = "피처 또는 연결 대상을 찾을 수 없음"),
        ],
    )
    @PatchMapping("/{featureId}/assign")
    fun assignSomethingToFeature(
        @Parameter(description = "피처 ID (UUID)", required = true) @PathVariable featureId: String,
        @Parameter(description = "할당 정보", required = true) @RequestBody @Valid assignDto: FeatureAssignDto,
        @Parameter(description = "이미 할당된 대상이 있을 경우 강제로 재할당할지 여부") @RequestParam(
            required = false,
            defaultValue = "false",
        ) force: Boolean,
    ): ResponseEntity<Unit> {
        featureService.assignSomethingToFeature(featureId, assignDto, force)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "피처 연결 해제", description = "특정 피처 연결을 해제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "연결 해제 성공",
                content = [Content(schema = Schema(implementation = FeatureResponse::class))],
            ), ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 피처에 할당되지 않음)"), ApiResponse(
                responseCode = "404",
                description = "피처를 찾을 수 없음",
            ),
        ],
    )
    @DeleteMapping("/{featureId}/revoke")
    fun removeSomethingFromFeature(
        @Parameter(description = "피처 ID (UUID)", required = true) @PathVariable featureId: String,
        @Parameter(description = "해제 정보", required = true) @RequestBody @Valid assignDto: FeatureAssignDto,
    ): ResponseEntity<Unit> {
        featureService.removeSomethingFromFeature(featureId, assignDto)
        return ResponseEntity.noContent().build()
    }
}
