package com.pluxity.facility

import com.pluxity.facility.dto.*
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
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/facilities")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Facility Controller", description = "시설 관리 API")
class FacilityController {
    private val facilityService: FacilityService? = null

    @get:GetMapping
    @get:ApiResponses(
        value = [ApiResponse(
            responseCode = "200",
            description = "목록 조회 성공"
        ), ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = Content(
                mediaType = "application/json",
                schema = Schema(implementation = ErrorResponseBody::class)
            )
        )]
    )
    @get:Operation(summary = "시설 목록 조회", description = "모든 시설 목록을 조회합니다")
    val facilities: ResponseEntity<DataResponseBody<MutableList<FacilityResponse?>?>?>
        get() = ResponseEntity.ok<DataResponseBody<MutableList<FacilityResponse?>?>?>(
            DataResponseBody.of<MutableList<FacilityResponse?>?>(
                facilityService!!.findAllFacilities()
            )
        )

    @Operation(summary = "시설 도면 정보 수정", description = "시설 도면 정보를 수정합니다")
    @ApiResponses(
        value = [ApiResponse(responseCode = "204", description = "도면 수정 성공"), ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(
            responseCode = "404",
            description = "시설을 찾을 수 없음",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(responseCode = "500", description = "서버 오류", content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class)))]
    )
    @PatchMapping("/{facilityId}/drawing")
    fun patch(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long?,
        @Parameter(description = "도면 수정 정보", required = true) @RequestBody request: @Valid FacilityDrawingUpdateRequest
    ): ResponseEntity<Void?> {
        facilityService!!.updateDrawingFile(facilityId, request)
        return ResponseEntity.noContent().build<Void?>()
    }

    @Operation(summary = "시설 위치 정보 수정", description = "시설 위치 정보를 수정합니다")
    @ApiResponses(
        value = [ApiResponse(responseCode = "204", description = "위치 정보 수정 성공"), ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(
            responseCode = "404",
            description = "시설을 찾을 수 없음",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(responseCode = "500", description = "서버 오류", content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class)))]
    )
    @PutMapping("/{facilityId}/location")
    fun patchLocation(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long?,
        @Parameter(description = "위치 수정 정보", required = true) @RequestBody request: @Valid FacilityLocationUpdateRequest
    ): ResponseEntity<Void?> {
        facilityService!!.updateLocation(facilityId, request)
        return ResponseEntity.noContent().build<Void?>()
    }

    @Operation(summary = "시설 경로 정보 등록", description = "시설 경로 정보를 등록합니다")
    @ApiResponses(
        value = [ApiResponse(responseCode = "204", description = "경로 정보 등록 성공"), ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(
            responseCode = "404",
            description = "시설을 찾을 수 없음",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(responseCode = "500", description = "서버 오류", content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class)))]
    )
    @PostMapping("/{facilityId}/path")
    fun addPath(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long?,
        @Parameter(description = "경로 정보", required = true) @RequestBody request: @Valid FacilityPathSaveRequest
    ): ResponseEntity<Void?> {
        facilityService!!.savePath(facilityId, request)
        return ResponseEntity.noContent().build<Void?>()
    }

    @Operation(summary = "시설 경로 정보 수정", description = "시설 경로 정보를 수정합니다")
    @ApiResponses(
        value = [ApiResponse(responseCode = "204", description = "경로 정보 수정 성공"), ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(
            responseCode = "404",
            description = "시설을 찾을 수 없음",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(responseCode = "500", description = "서버 오류", content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class)))]
    )
    @PatchMapping("/{facilityId}/path/{pathId}")
    fun updatePath(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long?,
        @Parameter(description = "경로 ID", required = true) @PathVariable pathId: Long?,
        @Parameter(description = "경로 정보", required = true) @RequestBody request: @Valid FacilityPathUpdateRequest
    ): ResponseEntity<Void?> {
        facilityService!!.updatePath(facilityId, pathId, request)
        return ResponseEntity.noContent().build<Void?>()
    }

    @Operation(summary = "시설 경로 정보 삭제", description = "시설 경로 정보를 삭제합니다")
    @ApiResponses(
        value = [ApiResponse(responseCode = "204", description = "경로 정보 삭제 성공"), ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(
            responseCode = "404",
            description = "시설을 찾을 수 없음",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(responseCode = "500", description = "서버 오류", content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class)))]
    )
    @DeleteMapping("/{facilityId}/path/{pathId}")
    fun deletePath(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long?,
        @Parameter(description = "경로 ID", required = true) @PathVariable pathId: Long?
    ): ResponseEntity<Void?> {
        facilityService!!.deletePath(facilityId, pathId)
        return ResponseEntity.noContent().build<Void?>()
    }

    @Operation(summary = "시설 이력 조회", description = "특정 ID를 가진 시설의 이력 목록을 조회합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "200", description = "이력 조회 성공"), ApiResponse(
            responseCode = "404",
            description = "해당 ID의 시설을 찾을 수 없음",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(responseCode = "500", description = "서버 오류", content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class)))]
    )
    @GetMapping("/{id}/history")
    fun getFacilityHistoryById(
        @PathVariable id: Long?
    ): ResponseEntity<DataResponseBody<MutableList<FacilityHistoryResponse?>?>?> {
        return ResponseEntity.ok<DataResponseBody<MutableList<FacilityHistoryResponse?>?>?>(
            DataResponseBody.of<MutableList<FacilityHistoryResponse?>?>(
                facilityService!!.findFacilityHistories(
                    id
                )
            )
        )
    }

    @Operation(summary = "시설 층 정보 수정", description = "시설 층 정보를 수정합니다")
    @ApiResponses(
        value = [ApiResponse(responseCode = "204", description = "층 정보 수정 성공"), ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(
            responseCode = "404",
            description = "시설을 찾을 수 없음",
            content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))
        ), ApiResponse(responseCode = "500", description = "서버 오류", content = Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class)))]
    )
    @PatchMapping("/{facilityId}/floors")
    fun patchFloors(
        @Parameter(description = "시설 ID", required = true) @PathVariable facilityId: Long?,
        @Parameter(description = "층 수정 정보", required = true) @RequestBody request: @Valid FacilityFloorUpdateRequest
    ): ResponseEntity<Void?> {
        facilityService!!.updateFloor(facilityId, request)
        return ResponseEntity.noContent().build<Void?>()
    }
}
