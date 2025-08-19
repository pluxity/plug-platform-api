package com.pluxity.station

import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.global.response.DataResponseBody
import com.pluxity.global.response.ErrorResponseBody
import com.pluxity.station.dto.StationCreateRequest
import com.pluxity.station.dto.StationResponse
import com.pluxity.station.dto.StationResponseWithFeature
import com.pluxity.station.dto.StationUpdateRequest
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

@RestController
@RequestMapping("/stations")
@Tag(name = "Station Controller", description = "스테이션 관리 API")
class StationController(
    private val service: StationService,
) {
    @Operation(summary = "스테이션 생성", description = "새로운 스테이션을 생성합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "스테이션 생성 성공",
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
    @ResponseCreated(path = "/stations/{id}")
    fun create(
        @Parameter(description = "스테이션 생성 정보", required = true) @RequestBody request: @Valid StationCreateRequest,
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
    @Operation(summary = "스테이션 목록 조회", description = "모든 스테이션 목록을 조회합니다")
    fun get(): ResponseEntity<DataResponseBody<List<StationResponse>>> = ResponseEntity.ok(DataResponseBody.of(service.findAll()))

    @Operation(summary = "스테이션 상세 조회", description = "ID로 특정 스테이션의 상세 정보를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "스테이션 조회 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "스테이션을 찾을 수 없음",
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
        @Parameter(description = "스테이션 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<DataResponseBody<StationResponse>> = ResponseEntity.ok(DataResponseBody.of(service.findById(id)))

    @Operation(summary = "스테이션 수정", description = "기존 스테이션의 정보를 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "스테이션 수정 성공",
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
                description = "스테이션을 찾을 수 없음",
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
    @PutMapping("/{id}")
    fun put(
        @Parameter(description = "스테이션 ID", required = true) @PathVariable id: Long,
        @Parameter(description = "스테이션 수정 정보", required = true) @RequestBody request: @Valid StationUpdateRequest,
    ): ResponseEntity<Void> {
        service.putUpdate(id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "스테이션 삭제", description = "ID로 스테이션을 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "스테이션 삭제 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "스테이션을 찾을 수 없음",
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
        @Parameter(description = "스테이션 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "스테이션에 노선 추가", description = "특정 스테이션에 노선을 추가합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "노선 추가 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "스테이션 또는 노선을 찾을 수 없음",
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
    @PostMapping("/{stationId}/lines/{lineId}")
    fun addLineToStation(
        @Parameter(description = "스테이션 ID", required = true) @PathVariable stationId: Long,
        @Parameter(description = "노선 ID", required = true) @PathVariable lineId: Long,
    ): ResponseEntity<Void> {
        service.addLineToStation(stationId, lineId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "스테이션에서 노선 제거", description = "특정 스테이션에서 노선을 제거합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "노선 제거 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "스테이션 또는 노선을 찾을 수 없음",
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
    @DeleteMapping("/{stationId}/lines/{lineId}")
    fun removeLineFromStation(
        @Parameter(description = "스테이션 ID", required = true) @PathVariable stationId: Long,
        @Parameter(description = "노선 ID", required = true) @PathVariable lineId: Long,
    ): ResponseEntity<Void> {
        service.removeLineFromStation(stationId, lineId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "스테이션의 피처 목록 조회", description = "특정 스테이션의 모든 피처 목록을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "피처 목록 조회 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "스테이션을 찾을 수 없음",
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
    @GetMapping("/{stationId}/features")
    fun getStationFeatures(
        @Parameter(description = "스테이션 ID", required = true) @PathVariable stationId: Long,
    ): ResponseEntity<DataResponseBody<StationResponseWithFeature>> =
        ResponseEntity.ok(DataResponseBody.of(service.findStationWithFeatures(stationId)))
}
