package com.pluxity.label3d

import com.pluxity.global.response.DataResponseBody
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
@RequestMapping("/label-3d")
@Tag(name = "Label3D", description = "Label3D 관리 API")
class Label3DController(
    private val label3DService: Label3DService,
) {
    @PostMapping
    @Operation(summary = "Label3D 생성", description = "새로운 Label3D를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Label3D 생성 성공",
                content = [Content(schema = Schema(implementation = Label3DResponse::class))],
            ), ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        ],
    )
    fun create(
        @RequestBody @Valid request: Label3DCreateRequest,
    ): ResponseEntity<DataResponseBody<Label3DResponse>> {
        val response = label3DService.createLabel3D(request)
        return ResponseEntity.ok(DataResponseBody(response))
    }

    @GetMapping
    @Operation(summary = "모든 Label3D 조회", description = "모든 Label3D를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    fun getAll(): ResponseEntity<DataResponseBody<List<Label3DResponse>>> {
        val responses = label3DService.getAllLabel3Ds()
        return ResponseEntity.ok(DataResponseBody(responses))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Label3D 조회", description = "ID로 특정 Label3D를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(schema = Schema(implementation = Label3DResponse::class))],
            ), ApiResponse(responseCode = "404", description = "Label3D를 찾을 수 없음"),
        ],
    )
    fun get(
        @Parameter(description = "Label3D ID") @PathVariable id: String,
    ): ResponseEntity<DataResponseBody<Label3DResponse>> {
        val response = label3DService.getLabel3DById(id)
        return ResponseEntity.ok(DataResponseBody(response))
    }

    @GetMapping("/facility/{facilityId}")
    @Operation(summary = "Facility별 Label3D 조회", description = "특정 Facility에 속한 모든 Label3D를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    fun getByFacilityId(
        @Parameter(description = "Facility ID") @PathVariable facilityId: Long,
    ): ResponseEntity<DataResponseBody<List<Label3DResponse>>> {
        val responses = label3DService.getLabel3DsByFacilityId(facilityId)
        return ResponseEntity.ok(DataResponseBody(responses))
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Label3D 수정", description = "Label3D를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "수정 성공",
                content = [Content(schema = Schema(implementation = Label3DResponse::class))],
            ), ApiResponse(responseCode = "404", description = "Label3D를 찾을 수 없음"),
        ],
    )
    fun update(
        @Parameter(description = "Label3D ID") @PathVariable id: String,
        @RequestBody @Valid request: Label3DUpdateRequest,
    ): ResponseEntity<Void> {
        label3DService.updateLabel3D(id, request)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Label3D 삭제", description = "Label3D를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공"), ApiResponse(
                responseCode = "404",
                description = "Label3D를 찾을 수 없음",
            ),
        ],
    )
    fun delete(
        @Parameter(description = "Label3D ID") @PathVariable id: String,
    ): ResponseEntity<Void> {
        label3DService.deleteLabel3D(id)
        return ResponseEntity.noContent().build()
    }
}
