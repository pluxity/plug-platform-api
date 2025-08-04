package com.pluxity.facility;

import com.pluxity.facility.dto.*;
import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ErrorResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/facilities")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Facility Controller", description = "시설 관리 API")
public class FacilityController {

    private final FacilityService facilityService;
    private final FacilityProviderService facilityProviderService;

    @Operation(summary = "시설 목록 조회", description = "모든 시설 목록을 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "목록 조회 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = FacilityResponse.class),
                                        examples =
                                                @ExampleObject(
                                                        value =
                                                                "{\"timestamp\": \"string\", \"status\": 0, \"message\": \"string\", \"data\":{\"buildings\": [{\"id\": 0, \"code\": \"string\", \"name\": \"string\", \"description\": \"string\", \"drawing\": {\"id\": 0, \"url\": \"string\", \"originalFileName\": \"string\", \"contentType\": \"string\", \"fileStatus\": \"string\", \"createdAt\": \"string\", \"createdBy\": \"string\", \"updatedAt\": \"string\", \"updatedBy\": \"string\"},\"thumbnail\": {\"id\": 0, \"url\": \"string\", \"originalFileName\": \"string\", \"contentType\": \"string\", \"fileStatus\": \"string\", \"createdAt\": \"string\", \"createdBy\": \"string\", \"updatedAt\": \"string\", \"updatedBy\": \"string\"},\"paths\": [{\"id\": 0, \"name\": \"string\", \"type\": \"string\", \"path\": \"string\"}],\"lon\": 0, \"lat\": 0, \"locationMeta\": \"string\", \"createdAt\": \"string\", \"createdBy\": \"string\", \"updatedAt\": \"string\", \"updatedBy\": \"string\"}],\"stations\": [],\"parks\": []}}"))),
                @ApiResponse(
                        responseCode = "500",
                        description = "서버 오류",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @GetMapping
    public ResponseEntity<DataResponseBody<Map<String, List<FacilityResponse>>>> getFacilities() {
        return ResponseEntity.ok(DataResponseBody.of(facilityProviderService.findAllFacilities()));
    }

    @Operation(summary = "시설 도면 정보 수정", description = "시설 도면 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "도면 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "시설을 찾을 수 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "서버 오류",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PatchMapping("/{facilityId}/drawing")
    public ResponseEntity<Void> patch(
            @Parameter(description = "시설물 ID", required = true) @PathVariable Long facilityId,
            @Parameter(description = "도면 수정 정보", required = true) @Valid @RequestBody
                    FacilityDrawingUpdateRequest request) {
        facilityService.updateDrawingFile(facilityId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "시설 위치 정보 수정", description = "시설 위치 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "위치 정보 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "시설을 찾을 수 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "서버 오류",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PutMapping("/{facilityId}/location")
    public ResponseEntity<Void> patchLocation(
            @Parameter(description = "시설물 ID", required = true) @PathVariable Long facilityId,
            @Parameter(description = "위치 수정 정보", required = true) @Valid @RequestBody
                    FacilityLocationUpdateRequest request) {
        facilityService.updateLocation(facilityId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "시설 경로 정보 등록", description = "시설 경로 정보를 등록합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "경로 정보 등록 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "시설을 찾을 수 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "서버 오류",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PostMapping("/{facilityId}/path")
    public ResponseEntity<Void> addPath(
            @Parameter(description = "시설물 ID", required = true) @PathVariable Long facilityId,
            @Parameter(description = "경로 정보", required = true) @Valid @RequestBody
                    FacilityPathSaveRequest request) {
        facilityService.savePath(facilityId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "시설 경로 정보 수정", description = "시설 경로 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "경로 정보 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "시설을 찾을 수 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "서버 오류",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PatchMapping("/{facilityId}/path/{pathId}")
    public ResponseEntity<Void> updatePath(
            @Parameter(description = "시설물 ID", required = true) @PathVariable Long facilityId,
            @Parameter(description = "경로 ID", required = true) @PathVariable Long pathId,
            @Parameter(description = "경로 정보", required = true) @Valid @RequestBody
                    FacilityPathUpdateRequest request) {
        facilityService.updatePath(facilityId, pathId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "시설 경로 정보 삭제", description = "시설 경로 정보를 삭제합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "경로 정보 삭제 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "시설을 찾을 수 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "서버 오류",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @DeleteMapping("/{facilityId}/path/{pathId}")
    public ResponseEntity<Void> deletePath(
            @Parameter(description = "시설물 ID", required = true) @PathVariable Long facilityId,
            @Parameter(description = "경로 ID", required = true) @PathVariable Long pathId) {
        facilityService.deletePath(facilityId, pathId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "시설물 이력 조회", description = "특정 ID를 가진 시설물의 이력 목록을 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "이력 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 시설물을 찾을 수 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "서버 오류",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @GetMapping("/{id}/history")
    public ResponseEntity<DataResponseBody<List<FacilityHistoryResponse>>> getFacilityHistoryById(
            @PathVariable Long id) {
        return ResponseEntity.ok(DataResponseBody.of(facilityService.findFacilityHistories(id)));
    }
}
