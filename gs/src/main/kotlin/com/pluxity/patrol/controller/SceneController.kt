package com.pluxity.patrol.controller

import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.global.response.ErrorResponseBody
import com.pluxity.patrol.dto.SceneCreateRequest
import com.pluxity.patrol.dto.SceneListResponse
import com.pluxity.patrol.dto.SceneResponse
import com.pluxity.patrol.dto.SceneUpdateRequest
import com.pluxity.patrol.service.SceneService
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
@RequestMapping("/facilities/{facilityId}/scenes")
@Tag(name = "Scene Controller", description = "씬 관리 API")
class SceneController(
    val sceneService: SceneService,
) {
    @Operation(summary = "씬 생성", description = "새로운 씬을 생성합니다. <br>sceneDeviceActionId가 null이면 신규 생성, 있으면 기존 것 업데이트")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "씬 생성 성공"),
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
    @ResponseCreated(path = "/scenes/{id}")
    fun createScene(
        @Parameter(description = "Facility ID", required = true) @PathVariable("facilityId") facilityId: Long,
        @Parameter(description = "씬 생성 정보", required = true) @RequestBody @Valid request: SceneCreateRequest,
    ): ResponseEntity<Long> = ResponseEntity.ok(sceneService.createScene(facilityId, request))

    @Operation(summary = "씬 상세 조회", description = "ID로 특정 씬의 상세 정보를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "씬 조회 성공"),
            ApiResponse(
                responseCode = "404",
                description = "씬을 찾을 수 없음",
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
    fun getScene(
        @Parameter(description = "Facility ID", required = true) @PathVariable("facilityId") facilityId: Long,
        @Parameter(description = "씬 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<SceneResponse> = ResponseEntity.ok(sceneService.getScene(facilityId, id))

    @Operation(summary = "시설별 씬 목록 조회", description = "시설 ID로 해당 시설의 씬 목록을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "목록 조회 성공"),
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
    @GetMapping
    fun getScenesByFacility(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long,
    ): ResponseEntity<List<SceneListResponse>> = ResponseEntity.ok(sceneService.getScenesByFacilityId(facilityId))

    @Operation(summary = "씬 수정", description = "기존 씬의 정보를 수정합니다. <br>sceneDeviceActionId가 null이면 신규 생성, 있으면 기존 것 업데이트")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "씬 수정 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "씬을 찾을 수 없음",
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
    fun updateScene(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long,
        @Parameter(description = "씬 ID", required = true) @PathVariable id: Long,
        @Parameter(description = "씬 수정 정보", required = true) @RequestBody @Valid request: SceneUpdateRequest,
    ): ResponseEntity<Void> {
        sceneService.updateScene(facilityId, id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "씬 삭제", description = "ID로 씬을 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "씬 삭제 성공"),
            ApiResponse(
                responseCode = "404",
                description = "씬을 찾을 수 없음",
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
    fun deleteScene(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long,
        @Parameter(description = "씬 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<Void> {
        sceneService.deleteScene(facilityId, id)
        return ResponseEntity.noContent().build()
    }
}
