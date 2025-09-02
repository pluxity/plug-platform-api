package com.pluxity.facility.category

import com.pluxity.facility.category.dto.FacilityCategoryAllResponse
import com.pluxity.facility.category.dto.FacilityCategoryCreateRequest
import com.pluxity.facility.category.dto.FacilityCategoryUpdateRequest
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
@RequestMapping("/facility-categories")
@RequiredArgsConstructor
@Tag(name = "Facility Category Controller", description = "시설 카테고리 관리 API")
class FacilityCategoryController {
    private val service: FacilityCategoryService? = null

    @Operation(summary = "시설 카테고리 생성", description = "새로운 시설 카테고리를 생성합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "201", description = "시설 카테고리 생성 성공", content = Content(mediaType = "application/json")), ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(responseCode = "500", description = "서버 오류", content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class)))]
    )
    @PostMapping
    @ResponseCreated(path = "/facility-categories/{id}")
    fun createFacilityCategory(
        @Parameter(description = "시설 카테고리 생성 정보", required = true) @RequestBody req: @Valid FacilityCategoryCreateRequest
    ): ResponseEntity<Long?> {
        return ResponseEntity.ok<Long?>(service!!.create(req))
    }

    @get:GetMapping
    @get:ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "시설 카테고리 목록 조회 성공"
        ), ApiResponse(
            responseCode = "400",
            description = "파라미터 오류",
            content = Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponseBody::class)
            )
        ), ApiResponse(
            responseCode = "404",
            description = "카테고리를 찾을 수 없음",
            content = Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponseBody::class)
            )
        ), ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponseBody::class)
            )
        )]
    )
    @get:Operation(summary = "시설 카테고리 목록 조회", description = "모든 시설 카테고리 목록을 조회합니다.")
    val facilityCategories: ResponseEntity<DataResponseBody<FacilityCategoryAllResponse?>?>
        get() = ResponseEntity.ok<DataResponseBody<FacilityCategoryAllResponse?>?>(DataResponseBody.of<FacilityCategoryAllResponse?>(service!!.findAll()))

    @Operation(summary = "시설 카테고리 수정", description = "ID를 기반으로 시설 카테고리를 수정합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "204", description = "시설 카테고리 수정 성공"), ApiResponse(
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
    fun patchFacilityCategory(
        @Parameter(description = "시설 카테고리 ID", required = true) @PathVariable id: Long?,
        @Parameter(description = "시설 카테고리 수정 정보", required = true) @RequestBody request: @Valid FacilityCategoryUpdateRequest
    ): ResponseEntity<Void?> {
        service!!.update(id, request)
        return ResponseEntity.noContent().build<Void?>()
    }

    @Operation(summary = "시설 카테고리 삭제", description = "ID를 기반으로 시설 카테고리를 삭제합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "204", description = "시설 카테고리 삭제 성공"), ApiResponse(
            responseCode = "404",
            description = "카테고리를 찾을 수 없음",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(responseCode = "500", description = "서버 오류", content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class)))]
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "시설 카테고리 ID", required = true) @PathVariable id: Long?
    ): ResponseEntity<Void?> {
        service!!.delete(id)
        return ResponseEntity.noContent().build<Void?>()
    }
}
