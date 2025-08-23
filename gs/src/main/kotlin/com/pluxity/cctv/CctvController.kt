package com.pluxity.cctv

import com.pluxity.cctv.dto.CctvCreateRequest
import com.pluxity.cctv.dto.CctvResponse
import com.pluxity.cctv.dto.CctvUpdateRequest
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

@RestController
@RequestMapping("/cctvs")
@Tag(name = "CCTV Controller", description = "CCTV 관리 API")
class CctvController(
    private val cctvService: CctvService,
) {
    @Operation(summary = "CCTV 생성", description = "새로운 CCTV를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "CCTV 생성 성공",
            ), ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PostMapping
    @ResponseCreated(path = "/cctvs/{id}")
    fun create(
        @Parameter(description = "CCTV 생성 정보", required = true) @RequestBody request: @Valid CctvCreateRequest,
    ): ResponseEntity<String> {
        val id = cctvService.create(request)
        return ResponseEntity.ok(id)
    }

    @GetMapping
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "목록 조회 성공",
            ),
        ],
    )
    @Operation(summary = "CCTV 목록 조회", description = "모든 CCTV 목록을 조회합니다.")
    fun getAll(): ResponseEntity<DataResponseBody<List<CctvResponse>>> = ResponseEntity.ok(DataResponseBody.of(cctvService.findAll()))

    @Operation(summary = "CCTV 상세 조회", description = "ID로 특정 CCTV의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "CCTV 조회 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 디바이스를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @GetMapping("/{id}")
    fun getById(
        @Parameter(description = "CCTV ID", required = true) @PathVariable id: String,
    ): ResponseEntity<DataResponseBody<CctvResponse>> = ResponseEntity.ok(DataResponseBody.of(cctvService.getById(id)))

    @Operation(summary = "CCTV 정보 수정", description = "ID로 특정 CCTV의 정보를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "CCTV 수정 성공",
            ), ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 CCTV를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "CCTV ID", required = true) @PathVariable id: String,
        @Parameter(description = "CCTV 수정 정보", required = true) @RequestBody request: @Valid CctvUpdateRequest,
    ): ResponseEntity<Void> {
        cctvService.update(id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "CCTV 삭제", description = "ID로 특정 CCTV를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "CCTV 삭제 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 CCTV를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "CCTV ID", required = true) @PathVariable id: String,
    ): ResponseEntity<Void> {
        cctvService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
