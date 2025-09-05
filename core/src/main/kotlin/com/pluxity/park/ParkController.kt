package com.pluxity.park

import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.global.response.DataResponseBody
import com.pluxity.global.response.ErrorResponseBody
import com.pluxity.park.dto.ParkCreateRequest
import com.pluxity.park.dto.ParkResponse
import com.pluxity.park.dto.ParkUpdateRequest
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
@RequestMapping("/facilities/parks")
@Tag(name = "Park Controller", description = "공원 관리 API")
class ParkController(
    private val parkService: ParkService,
) {
    @Operation(summary = "공원 생성", description = "새로운 공원을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "공원 생성 성공",
            ), ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PostMapping
    @ResponseCreated(path = "/parks/{id}")
    fun create(
        @Parameter(description = "공원 생성 정보", required = true) @RequestBody request: @Valid ParkCreateRequest,
    ): ResponseEntity<Long> = ResponseEntity.ok(parkService.save(request))

    @GetMapping
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "목록 조회 성공",
            ),
        ],
    )
    @Operation(summary = "공원 목록 조회", description = "모든 공원 목록을 조회합니다.")
    fun getAll(): ResponseEntity<DataResponseBody<List<ParkResponse>>> = ResponseEntity.ok(DataResponseBody(parkService.findAll()))

    @Operation(summary = "공원 상세 조회", description = "ID로 특정 공원의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "공원 조회 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 공원을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @GetMapping("/{id}")
    fun get(
        @Parameter(description = "공원 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<DataResponseBody<ParkResponse>> =
        ResponseEntity.ok(
            DataResponseBody(
                parkService.findById(
                    id,
                ),
            ),
        )

    @Operation(summary = "공원 정보 수정", description = "ID로 특정 공원의 정보를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "공원 수정 성공",
            ), ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 공원을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PutMapping("/{id}")
    fun put(
        @Parameter(description = "공원 ID", required = true) @PathVariable id: Long,
        @Parameter(description = "공원 수정 정보", required = true) @RequestBody request: @Valid ParkUpdateRequest,
    ): ResponseEntity<Void> {
        parkService.update(id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "공원 삭제", description = "ID로 특정 공원을 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "공원 삭제 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 공원을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "공원 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<Void> {
        parkService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
