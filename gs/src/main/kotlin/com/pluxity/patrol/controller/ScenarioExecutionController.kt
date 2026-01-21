package com.pluxity.patrol.controller

import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.patrol.dto.ScenarioExecutionFailRequest
import com.pluxity.patrol.service.ScenarioExecutionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/scenario-executions")
@Tag(name = "ScenarioExecution Controller", description = "시나리오 실행 API")
class ScenarioExecutionController(
    private val scenarioExecutionService: ScenarioExecutionService,
) {
    @ResponseCreated(path = "/scenario-executions/{id}")
    @PostMapping("/start/{scenarioId}")
    @Operation(summary = "시나리오 수동 실행")
    fun start(
        @PathVariable scenarioId: Long,
    ): ResponseEntity<Long> = ResponseEntity.ok(scenarioExecutionService.start(scenarioId))

    @PostMapping("/{id}/complete")
    @Operation(summary = "시나리오 실행 완료")
    fun complete(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        scenarioExecutionService.complete(id)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/fail")
    @Operation(summary = "시나리오 실행 실패")
    fun fail(
        @PathVariable id: Long,
        @RequestBody request: ScenarioExecutionFailRequest,
    ): ResponseEntity<Unit> {
        scenarioExecutionService.fail(id, request.errorMessage)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "시나리오 실행 취소")
    fun cancel(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        scenarioExecutionService.cancel(id)
        return ResponseEntity.ok().build()
    }
}
