package com.pluxity.domains.device.controller;

import com.pluxity.domains.device.dto.NfluxCategoryGroupResponse;
import com.pluxity.domains.device.dto.NfluxCreateRequest;
import com.pluxity.domains.device.dto.NfluxResponse;
import com.pluxity.domains.device.dto.NfluxUpdateRequest;
import com.pluxity.domains.device.service.NfluxService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@Tag(name = "Device Controller", description = "디바이스 관리 API")
public class NfluxController {

    private final NfluxService service;

    @Operation(summary = "디바이스 생성", description = "새로운 디바이스를 생성합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "디바이스 생성 성공"),
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
    public ResponseEntity<String> create(
            @Parameter(description = "디바이스 생성 정보", required = true) @Valid @RequestBody
                    NfluxCreateRequest request) {

        String id = service.save(request);

        return ResponseEntity.ok(id);
    }

    @Operation(summary = "디바이스 목록 조회", description = "모든 디바이스 목록을 조회합니다")
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
    public ResponseEntity<DataResponseBody<List<NfluxResponse>>> getAll() {
        return ResponseEntity.ok(DataResponseBody.of(service.findAll()));
    }

    @Operation(summary = "디바이스 상세 조회", description = "ID로 특정 디바이스의 상세 정보를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "디바이스 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "디바이스를 찾을 수 없음",
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
    public ResponseEntity<DataResponseBody<NfluxResponse>> get(
            @Parameter(description = "디바이스 ID", required = true) @PathVariable String id) {
        return ResponseEntity.ok(DataResponseBody.of(service.findDeviceById(id)));
    }

    @Operation(summary = "디바이스 수정", description = "기존 디바이스의 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "디바이스 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "디바이스를 찾을 수 없음",
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
    public ResponseEntity<DataResponseBody<NfluxResponse>> update(
            @Parameter(description = "디바이스 ID", required = true) @PathVariable String id,
            @Parameter(description = "디바이스 수정 정보", required = true) @Valid @RequestBody
                    NfluxUpdateRequest request) {

        service.update(id, request);

        return ResponseEntity.ok(DataResponseBody.of(service.findDeviceById(id)));
    }

    @Operation(summary = "디바이스 삭제", description = "ID로 특정 디바이스를 삭제합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "디바이스 삭제 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "디바이스를 찾을 수 없음",
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(
            @Parameter(description = "디바이스 ID", required = true) @PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "디바이스에 카테고리 할당", description = "디바이스에 카테고리를 할당합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "카테고리 할당 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "디바이스 또는 카테고리를 찾을 수 없음",
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
    @PutMapping("/{deviceId}/category/{categoryId}")
    public ResponseEntity<DataResponseBody<NfluxResponse>> assignCategoryToDevice(
            @Parameter(description = "디바이스 ID", required = true) @PathVariable String deviceId,
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long categoryId) {
        NfluxResponse response = service.assignCategory(deviceId, categoryId);
        return ResponseEntity.ok(DataResponseBody.of(response));
    }

    @Operation(summary = "디바이스에서 카테고리 연결 해제", description = "디바이스에 할당된 카테고리와의 연결을 해제합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "카테고리 연결 해제 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청 (예: 디바이스에 카테고리가 할당되지 않음)",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "디바이스를 찾을 수 없음",
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
    @DeleteMapping("/{deviceId}/category")
    public ResponseEntity<DataResponseBody<NfluxResponse>> removeCategoryFromDevice(
            @Parameter(description = "디바이스 ID", required = true) @PathVariable String deviceId) {
        NfluxResponse response = service.removeCategory(deviceId);
        return ResponseEntity.ok(DataResponseBody.of(response));
    }

    @Operation(summary = "디바이스에 피처 할당", description = "디바이스에 특정 피처를 할당합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "피처 할당 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 피처가 이미 다른 디바이스에 할당됨)"),
                @ApiResponse(responseCode = "404", description = "디바이스 또는 피처를 찾을 수 없음")
            })
    @PutMapping("/{deviceId}/features/{featureId}")
    public ResponseEntity<DataResponseBody<NfluxResponse>> assignFeatureToDevice(
            @Parameter(description = "디바이스 ID", required = true) @PathVariable String deviceId,
            @Parameter(description = "피처 ID (UUID)", required = true) @PathVariable String featureId) {
        NfluxResponse response = service.assignFeatureToNflux(deviceId, featureId);
        return ResponseEntity.ok(DataResponseBody.of(response));
    }

    // Feature 연결 해제 API
    @Operation(summary = "디바이스에서 피처 연결 해제", description = "디바이스에 할당된 피처와의 연결을 해제합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "피처 연결 해제 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 디바이스에 피처가 할당되지 않음)"),
                @ApiResponse(responseCode = "404", description = "디바이스를 찾을 수 없음")
            })
    @DeleteMapping("/{deviceId}/features")
    public ResponseEntity<DataResponseBody<NfluxResponse>> removeFeatureFromDevice(
            @Parameter(description = "디바이스 ID", required = true) @PathVariable String deviceId) {
        NfluxResponse response = service.removeFeatureFromNflux(deviceId);
        return ResponseEntity.ok(DataResponseBody.of(response));
    }

    @Operation(
            summary = "스테이션 ID로 디바이스 조회 및 카테고리별 그룹화",
            description = "특정 스테이션에 연결된 디바이스를 조회하고 카테고리별로 그룹화합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "조회 성공"),
                @ApiResponse(responseCode = "404", description = "스테이션을 찾을 수 없음"),
                @ApiResponse(responseCode = "500", description = "서버 오류")
            })
    @GetMapping("/station/{stationCode}/grouped")
    public ResponseEntity<DataResponseBody<List<NfluxCategoryGroupResponse>>>
            getDevicesByStationGroupedByCategory(
                    @Parameter(description = "스테이션 ID", required = true) @PathVariable String stationCode) {
        List<NfluxCategoryGroupResponse> response =
                service.findByStationCodeGroupByCategory(stationCode);
        return ResponseEntity.ok(DataResponseBody.of(response));
    }
}
