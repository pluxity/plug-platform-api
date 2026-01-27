package com.pluxity.patrol.controller

import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.global.response.ErrorResponseBody
import com.pluxity.patrol.dto.ScenarioExecutionDetailResponse
import com.pluxity.patrol.dto.ScenarioExecutionFailRequest
import com.pluxity.patrol.dto.ScenarioExecutionResponse
import com.pluxity.patrol.dto.ScenarioExecutionSearchRequest
import com.pluxity.patrol.service.ScenarioExecutionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "ScenarioExecution Controller", description = "시나리오 실행 이력 API")
class ScenarioExecutionController(
    private val scenarioExecutionService: ScenarioExecutionService,
) {
    @ResponseCreated(path = "/scenario-executions/{id}")
    @PostMapping("/scenario-executions/start/{scenarioId}")
    @Operation(summary = "시나리오 수동 실행")
    fun startManually(
        @PathVariable scenarioId: Long,
    ): ResponseEntity<Long> = ResponseEntity.ok(scenarioExecutionService.startManually(scenarioId))

    @PatchMapping("/scenario-executions/{id}/complete")
    @Operation(summary = "시나리오 실행 완료")
    fun complete(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        scenarioExecutionService.complete(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/scenario-executions/{id}/fail")
    @Operation(summary = "시나리오 실행 실패")
    fun fail(
        @PathVariable id: Long,
        @RequestBody request: ScenarioExecutionFailRequest,
    ): ResponseEntity<Void> {
        scenarioExecutionService.fail(id, request.errorMessage)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/scenario-executions/{id}/cancel")
    @Operation(summary = "시나리오 실행 취소")
    fun cancel(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        scenarioExecutionService.cancel(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "시나리오 실행 상세 조회", description = "특정 시나리오 실행의 상세 정보와 씬 실행 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(
                responseCode = "404",
                description = "ScenarioExecution을 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @GetMapping("/scenario-executions/{id}")
    fun findById(
        @Parameter(description = "시나리오 실행 ID", required = true) @PathVariable("id") id: Long,
    ): ResponseEntity<ScenarioExecutionDetailResponse> = ResponseEntity.ok(scenarioExecutionService.findById(id))

    @Operation(summary = "시나리오별 실행 이력 조회", description = "특정 시나리오의 실행 이력을 검색/필터링하여 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
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
    @GetMapping("/scenarios/{scenarioId}/executions")
    fun findByScenarioId(
        @Parameter(description = "시나리오 ID", required = true) @PathVariable("scenarioId") scenarioId: Long,
        request: ScenarioExecutionSearchRequest,
    ): ResponseEntity<List<ScenarioExecutionResponse>> = ResponseEntity.ok(scenarioExecutionService.findByScenarioId(scenarioId, request))

    @Operation(summary = "시설별 실행 이력 조회", description = "특정 시설의 모든 시나리오 실행 이력을 검색/필터링하여 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
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
    @GetMapping("/facilities/{facilityId}/executions")
    fun findByFacilityId(
        @Parameter(description = "시설 ID", required = true) @PathVariable("facilityId") facilityId: Long,
        request: ScenarioExecutionSearchRequest,
    ): ResponseEntity<List<ScenarioExecutionResponse>> = ResponseEntity.ok(scenarioExecutionService.findByFacilityId(facilityId, request))
}
