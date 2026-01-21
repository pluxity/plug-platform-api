package com.pluxity.patrol.controller

import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.global.response.ErrorResponseBody
import com.pluxity.patrol.dto.SceneExecutionFailRequest
import com.pluxity.patrol.dto.SceneExecutionStartRequest
import com.pluxity.patrol.service.SceneExecutionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/scene-executions")
@Tag(name = "SceneExecution Controller", description = "씬 실행 이력 API")
class SceneExecutionController(
    private val sceneExecutionService: SceneExecutionService,
) {
    @ResponseCreated(path = "/scene-executions/{id}")
    @PostMapping("/start")
    @Operation(summary = "씬 실행 시작", description = "새로운 SceneExecution을 생성하고 RUNNING 상태로 시작합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "생성 성공"),
            ApiResponse(
                responseCode = "404",
                description = "ScenarioExecution 또는 Scene을 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    fun start(
        @RequestBody request: SceneExecutionStartRequest,
    ): ResponseEntity<Long> = ResponseEntity.ok(sceneExecutionService.start(request.scenarioExecutionId, request.sceneId))

    @PostMapping("/{id}/complete")
    @Operation(summary = "씬 실행 완료", description = "씬 실행을 완료 상태로 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "완료 처리 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 상태 전이",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "SceneExecution을 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    fun complete(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        sceneExecutionService.complete(id)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/fail")
    @Operation(summary = "씬 실행 실패", description = "씬 실행을 실패 상태로 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "실패 처리 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 상태 전이",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "SceneExecution을 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    fun fail(
        @PathVariable id: Long,
        @RequestBody request: SceneExecutionFailRequest,
    ): ResponseEntity<Unit> {
        sceneExecutionService.fail(id, request.errorMessage)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/skip")
    @Operation(summary = "씬 건너뛰기", description = "씬 실행을 건너뛴 상태로 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "건너뛰기 처리 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 상태 전이",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "SceneExecution을 찾을 수 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    fun skip(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        sceneExecutionService.skip(id)
        return ResponseEntity.ok().build()
    }
}
