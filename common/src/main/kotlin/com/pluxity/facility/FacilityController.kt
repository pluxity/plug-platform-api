package com.pluxity.facility

import com.pluxity.facility.dto.FacilityDrawingUpdateRequest
import com.pluxity.facility.dto.FacilityFloorUpdateRequest
import com.pluxity.facility.dto.FacilityHistoryResponse
import com.pluxity.facility.dto.FacilityLocationUpdateRequest
import com.pluxity.facility.dto.FacilityPathSaveRequest
import com.pluxity.facility.dto.FacilityPathUpdateRequest
import com.pluxity.facility.dto.FacilityResponse
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
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/facilities")
@Tag(name = "Facility Controller", description = "시설 관리 API")
class FacilityController(
    private val facilityService: FacilityService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Operation(summary = "시설 목록 조회", description = "모든 시설 목록을 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "목록 조회 성공"),
            ApiResponse(
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
    @GetMapping
    fun getFacilities(): ResponseEntity<DataResponseBody<List<FacilityResponse>>> =
        ResponseEntity.ok(DataResponseBody(facilityService.findAllFacilities()))

    @Operation(summary = "시설 도면 정보 수정", description = "시설 도면 정보를 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "도면 수정 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "시설을 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
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
    @PatchMapping("/{facilityId}/drawing")
    fun patch(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long,
        @Parameter(description = "도면 수정 정보", required = true) @Valid @RequestBody
        request: FacilityDrawingUpdateRequest,
    ): ResponseEntity<Void> {
        facilityService.updateDrawingFile(facilityId, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "시설 위치 정보 수정", description = "시설 위치 정보를 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "위치 정보 수정 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "시설을 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
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
    @PutMapping("/{facilityId}/location")
    fun patchLocation(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long,
        @Parameter(description = "위치 수정 정보", required = true) @Valid @RequestBody
        request: FacilityLocationUpdateRequest,
    ): ResponseEntity<Void> {
        facilityService.updateLocation(facilityId, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "시설 경로 정보 등록", description = "시설 경로 정보를 등록합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "경로 정보 등록 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "시설을 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
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
    @PostMapping("/{facilityId}/path")
    fun addPath(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long,
        @Parameter(description = "경로 정보", required = true) @Valid @RequestBody
        request: FacilityPathSaveRequest,
    ): ResponseEntity<Void> {
        facilityService.savePath(facilityId, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "시설 경로 정보 수정", description = "시설 경로 정보를 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "경로 정보 수정 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "시설을 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
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
    @PatchMapping("/{facilityId}/path/{pathId}")
    fun updatePath(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long,
        @Parameter(description = "경로 ID", required = true) @PathVariable pathId: Long,
        @Parameter(description = "경로 정보", required = true) @Valid @RequestBody
        request: FacilityPathUpdateRequest,
    ): ResponseEntity<Void> {
        facilityService.updatePath(facilityId, pathId, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "시설 경로 정보 삭제", description = "시설 경로 정보를 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "경로 정보 삭제 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "시설을 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
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
    @DeleteMapping("/{facilityId}/path/{pathId}")
    fun deletePath(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long,
        @Parameter(description = "경로 ID", required = true) @PathVariable pathId: Long,
    ): ResponseEntity<Void> {
        facilityService.deletePath(facilityId, pathId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "시설 이력 조회", description = "특정 ID를 가진 시설의 이력 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "이력 조회 성공"),
            ApiResponse(
                responseCode = "404",
                description = "해당 ID의 시설을 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
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
    @GetMapping("/{id}/history")
    fun getFacilityHistoryById(
        @PathVariable id: Long,
    ): ResponseEntity<DataResponseBody<List<FacilityHistoryResponse>>> =
        ResponseEntity.ok(DataResponseBody(facilityService.findFacilityHistories(id)))

    @Operation(summary = "시설 층 정보 수정", description = "시설 층 정보를 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "층 정보 수정 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "시설을 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
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
    @PatchMapping("/{facilityId}/floors")
    fun patchFloors(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long,
        @Parameter(description = "층 수정 정보", required = true) @Valid @RequestBody
        request: FacilityFloorUpdateRequest,
    ): ResponseEntity<Void> {
        facilityService.updateFloor(facilityId, request)
        return ResponseEntity.noContent().build()
    }
}
