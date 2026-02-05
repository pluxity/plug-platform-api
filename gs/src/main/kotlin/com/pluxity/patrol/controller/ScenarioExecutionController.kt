package com.pluxity.patrol.controller

import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.global.response.ErrorResponseBody
import com.pluxity.patrol.constant.ScenarioExecutionStatus
import com.pluxity.patrol.dto.ScenarioExecutionDetailResponse
import com.pluxity.patrol.dto.ScenarioExecutionFailRequest
import com.pluxity.patrol.dto.ScenarioExecutionResponse
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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RequestMapping("/scenario-executions")
@RestController
@Tag(name = "ScenarioExecution Controller", description = "시나리오 실행 이력 API")
class ScenarioExecutionController(
    private val scenarioExecutionService: ScenarioExecutionService,
) {
    @ResponseCreated(path = "/scenario-executions/{id}")
    @PostMapping("/start/{scenarioId}")
    @Operation(summary = "시나리오 수동 실행", description = "특정 시나리오를 수동으로 즉시 실행합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "시나리오 실행 시작 성공"),
            ApiResponse(
                responseCode = "404",
                description = "시나리오를 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    fun startManually(
        @Parameter(description = "시나리오 ID", required = true) @PathVariable scenarioId: Long,
    ): ResponseEntity<Long> = ResponseEntity.ok(scenarioExecutionService.startManually(scenarioId))

    @PatchMapping("/{id}/complete")
    @Operation(summary = "시나리오 실행 완료", description = "진행 중인 시나리오 실행을 완료 상태로 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "시나리오 실행 완료 처리 성공"),
            ApiResponse(
                responseCode = "404",
                description = "시나리오 실행을 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    fun complete(
        @Parameter(description = "시나리오 실행 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<Void> {
        scenarioExecutionService.complete(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/fail")
    @Operation(summary = "시나리오 실행 실패", description = "진행 중인 시나리오 실행을 실패 상태로 변경하고 오류 메시지를 기록합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "시나리오 실행 실패 처리 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "시나리오 실행을 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    fun fail(
        @Parameter(description = "시나리오 실행 ID", required = true) @PathVariable id: Long,
        @Parameter(description = "실패 정보 (오류 메시지)", required = true) @RequestBody request: ScenarioExecutionFailRequest,
    ): ResponseEntity<Void> {
        scenarioExecutionService.fail(id, request.errorMessage)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "시나리오 실행 취소", description = "진행 중인 시나리오 실행을 취소 상태로 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "시나리오 실행 취소 성공"),
            ApiResponse(
                responseCode = "404",
                description = "시나리오 실행을 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    fun cancel(
        @Parameter(description = "시나리오 실행 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<Void> {
        scenarioExecutionService.cancel(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/running")
    @Operation(summary = "시나리오 실행 중", description = "대기 중인 시나리오 실행을 진행 중 상태로 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "시나리오 실행 상태 변경 성공"),
            ApiResponse(
                responseCode = "404",
                description = "시나리오 실행을 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    fun running(
        @Parameter(description = "시나리오 실행 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<Void> {
        scenarioExecutionService.running(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "시나리오 실행 이력 상세 조회", description = "특정 시나리오 실행의 상세 정보와 씬 실행 목록을 조회합니다.")
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
    @GetMapping("/{id}")
    fun findById(
        @Parameter(description = "시나리오 실행 ID", required = true) @PathVariable("id") id: Long,
    ): ResponseEntity<ScenarioExecutionDetailResponse> = ResponseEntity.ok(scenarioExecutionService.findById(id))

    @Operation(summary = "시나리오 실행 이력 목록 조회", description = "시설, 시나리오, 상태, 기간 조건으로 실행 이력을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(
                responseCode = "404",
                description = "시설 또는 시나리오를 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @GetMapping
    fun findByFilters(
        @Parameter(description = "시설 ID") @RequestParam(required = false) facilityId: Long?,
        @Parameter(description = "시나리오 ID") @RequestParam(required = false) scenarioId: Long?,
        @Parameter(description = "실행 상태") @RequestParam(required = false) status: ScenarioExecutionStatus?,
        @Parameter(description = "조회 시작일 (yyyy-MM-dd)") @RequestParam(required = false) startDate: LocalDate?,
        @Parameter(description = "조회 종료일 (yyyy-MM-dd)") @RequestParam(required = false) endDate: LocalDate?,
    ): ResponseEntity<List<ScenarioExecutionResponse>> =
        ResponseEntity.ok(scenarioExecutionService.findByFilters(facilityId, scenarioId, status, startDate, endDate))
}
