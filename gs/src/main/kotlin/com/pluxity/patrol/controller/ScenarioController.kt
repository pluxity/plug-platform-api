package com.pluxity.patrol.controller

import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.global.response.ErrorResponseBody
import com.pluxity.patrol.dto.ScenarioCreateRequest
import com.pluxity.patrol.dto.ScenarioListResponse
import com.pluxity.patrol.dto.ScenarioResponse
import com.pluxity.patrol.dto.ScenarioUpdateRequest
import com.pluxity.patrol.service.ScenarioService
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
@RequestMapping("/facilities/{facilityId}/scenarios")
@Tag(name = "Scenario Controller", description = "시나리오 관리 API")
class ScenarioController(
    val scenarioService: ScenarioService,
) {
    @Operation(summary = "시나리오 생성", description = "새로운 시나리오를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "시나리오 생성 성공"),
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
    @ResponseCreated("/scenarios/{id}")
    fun createScenario(
        @Parameter(description = "시설 ID", required = true) @PathVariable("facilityId") facilityId: Long,
        @Parameter(description = "시나리오 생성 정보", required = true) @RequestBody @Valid request: ScenarioCreateRequest,
    ): ResponseEntity<Long> = ResponseEntity.ok(scenarioService.createScenario(facilityId, request))

    @Operation(summary = "시나리오 상세 조회", description = "ID로 특정 시나리오의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "시나리오 조회 성공"),
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
    @GetMapping("/{id}")
    fun getScenario(
        @Parameter(description = "시설 ID", required = true) @PathVariable("facilityId") facilityId: Long,
        @Parameter(description = "시나리오 ID", required = true) @PathVariable("id") id: Long,
    ): ResponseEntity<ScenarioResponse> = ResponseEntity.ok(scenarioService.getScenario(facilityId, id))

    @Operation(summary = "시설별 시나리오 목록 조회", description = "시설 ID로 해당 시설의 시나리오 목록을 조회합니다.")
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
    fun getScenariosByFacility(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long,
    ): ResponseEntity<List<ScenarioListResponse>> = ResponseEntity.ok(scenarioService.getScenarioByFacilityId(facilityId))

    @Operation(summary = "시나리오 수정", description = "기존 시나리오의 정보를 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "시나리오 수정 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
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
    @PutMapping("/{id}")
    fun updateScenario(
        @Parameter(description = "시설 ID", required = true) @PathVariable("facilityId") facilityId: Long,
        @Parameter(description = "시나리오 ID", required = true) @PathVariable("id") id: Long,
        @Parameter(description = "시나리오 수정 정보", required = true) @RequestBody @Valid request: ScenarioUpdateRequest,
    ): ResponseEntity<Void> {
        scenarioService.updateScenario(facilityId, id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "시나리오 삭제", description = "ID로 시나리오를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "시나리오 삭제 성공"),
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
    @DeleteMapping("/{id}")
    fun deleteScenario(
        @Parameter(description = "시설 ID", required = true) @PathVariable("facilityId") facilityId: Long,
        @Parameter(description = "시나리오 ID", required = true) @PathVariable("id") id: Long,
    ): ResponseEntity<Void> {
        scenarioService.deleteScenario(facilityId, id)
        return ResponseEntity.noContent().build()
    }
}
