package com.pluxity.facility.building;

import com.pluxity.facility.building.dto.BuildingCreateRequest;
import com.pluxity.facility.building.dto.BuildingResponse;
import com.pluxity.facility.building.dto.BuildingUpdateRequest;
import com.pluxity.facility.facility.dto.FacilityHistoryResponse;
import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ErrorResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/buildings")
@RequiredArgsConstructor
@Tag(name = "Building Controller", description = "건물 관리 API")
public class BuildingController {

    private final BuildingService service;

    @Operation(summary = "건물 생성", description = "새로운 건물을 생성합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "건물 생성 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
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
    @PostMapping
    @ResponseCreated
    public ResponseEntity<Long> create(
            @Parameter(description = "건물 생성 정보", required = true) @Valid @RequestBody
                    BuildingCreateRequest request) {

        Long id = service.save(request);

        return ResponseEntity.ok(id);
    }

    @Operation(summary = "건물 목록 조회", description = "모든 건물 목록을 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
                @ApiResponse(
                        responseCode = "500",
                        description = "서버 오류",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @GetMapping
    public ResponseEntity<DataResponseBody<List<BuildingResponse>>> getAll() {
        return ResponseEntity.ok(DataResponseBody.of(service.findAll()));
    }

    @Operation(summary = "건물 상세 조회", description = "ID로 특정 건물의 상세 정보를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "건물 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "건물을 찾을 수 없음",
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
    @GetMapping("/{id}")
    public ResponseEntity<DataResponseBody<BuildingResponse>> get(
            @Parameter(description = "건물 ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(DataResponseBody.of(service.findById(id)));
    }

    @Operation(summary = "특정 건물 이력 조회", description = "특정 ID를 가진 건물의 이력 목록을 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "이력 조회 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = DataResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 건물을 찾을 수 없음",
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
    public ResponseEntity<DataResponseBody<List<FacilityHistoryResponse>>> getBuildingHistoryById(
            @PathVariable Long id) {
        return ResponseEntity.ok(DataResponseBody.of(service.findFacilityHistories(id)));
    }

    @Operation(summary = "건물 수정", description = "기존 건물의 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "건물 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "건물을 찾을 수 없음",
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
    @PatchMapping("/{id}")
    public ResponseEntity<Void> patch(
            @Parameter(description = "건물 ID", required = true) @PathVariable Long id,
            @Parameter(description = "건물 수정 정보", required = true) @Valid @RequestBody
                    BuildingUpdateRequest request) {
        service.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "건물 삭제", description = "ID로 건물을 삭제합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "건물 삭제 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "건물을 찾을 수 없음",
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "건물 ID", required = true) @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
