package com.pluxity.station

import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.global.response.DataResponseBody
import com.pluxity.global.response.ErrorResponseBody
import com.pluxity.station.dto.LineCreateRequest
import com.pluxity.station.dto.LineResponse
import com.pluxity.station.dto.LineUpdateRequest
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

@RestController
@RequestMapping("/lines")
@Tag(name = "Line Controller", description = "호선 관리 API")
class LineController(
    private val service: LineService,
) {
    @Operation(summary = "호선 생성", description = "새로운 호선을 생성합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "호선 생성 성공",
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
    @ResponseCreated
    fun create(
        @Parameter(description = "호선 생성 정보", required = true) @RequestBody request: @Valid LineCreateRequest,
    ): ResponseEntity<Long> = ResponseEntity.ok(service.save(request))

    @GetMapping
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "목록 조회 성공",
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
    @Operation(summary = "호선 목록 조회", description = "모든 호선 목록을 조회합니다")
    fun get(): ResponseEntity<DataResponseBody<List<LineResponse>>> = ResponseEntity.ok(DataResponseBody.of(service.findAll()))

    @Operation(summary = "호선 상세 조회", description = "ID로 특정 호선의 상세 정보를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "호선 조회 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "호선을 찾을 수 없음",
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
    @GetMapping("/{id}")
    fun get(
        @Parameter(description = "호선 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<DataResponseBody<LineResponse>> = ResponseEntity.ok(DataResponseBody.of(service.findById(id)))

    @Operation(summary = "호선에 속한 역 목록 조회", description = "특정 호선에 속한 역 목록을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "역 목록 조회 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "호선을 찾을 수 없음",
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
    @GetMapping("/{id}/stations")
    fun getStations(
        @Parameter(description = "호선 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<DataResponseBody<List<Long>>> = ResponseEntity.ok(DataResponseBody.of(service.findStationsByLineId(id)))

    @Operation(summary = "호선 수정", description = "기존 호선의 정보를 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "호선 수정 성공",
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
                description = "호선을 찾을 수 없음",
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
    @PatchMapping("/{id}")
    fun update(
        @Parameter(description = "호선 ID", required = true) @PathVariable id: Long,
        @Parameter(description = "호선 수정 정보", required = true) @RequestBody request: @Valid LineUpdateRequest,
    ): ResponseEntity<Void> {
        service.update(id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "호선 삭제", description = "ID로 호선을 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "호선 삭제 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "호선을 찾을 수 없음",
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
    fun delete(
        @Parameter(description = "호선 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}
