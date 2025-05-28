package com.pluxity.feature.controller;

import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.service.FeatureService;
import com.pluxity.global.response.ErrorResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/features")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Feature Controller", description = "피처 관리 API")
public class FeatureController {

    private final FeatureService featureService;

    @Operation(summary = "피처 생성", description = "새로운 피처를 생성합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "피처 생성 성공"),
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
    public ResponseEntity<FeatureResponse> createFeature(
            @Parameter(description = "피처 생성 정보", required = true) @Valid @RequestBody
                    FeatureCreateRequest request) {
        FeatureResponse response = featureService.createFeature(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "피처 목록 조회", description = "모든 피처 목록을 조회합니다")
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
    public ResponseEntity<List<FeatureResponse>> getFeatures() {
        List<FeatureResponse> responses = featureService.getFeatures();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "피처 상세 조회", description = "ID로 특정 피처의 상세 정보를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "피처 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "피처를 찾을 수 없음",
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
    public ResponseEntity<FeatureResponse> getFeature(
            @Parameter(description = "피처 ID", required = true) @PathVariable String id) {
        FeatureResponse response = featureService.getFeature(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "피처 정보 수정", description = "ID로 피처 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "피처 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "피처를 찾을 수 없음",
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
    @PutMapping("/{id}")
    public ResponseEntity<FeatureResponse> updateFeature(
            @Parameter(description = "피처 ID", required = true) @PathVariable String id,
            @Parameter(description = "피처 수정 정보", required = true) @Valid @RequestBody
                    FeatureUpdateRequest request) {
        FeatureResponse response = featureService.updateFeature(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "피처 위치 수정", description = "ID로 피처의 위치 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "위치 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "피처를 찾을 수 없음",
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
    @PatchMapping("/{id}/position")
    public ResponseEntity<FeatureResponse> updatePosition(
            @Parameter(description = "피처 ID", required = true) @PathVariable String id,
            @Parameter(description = "위치 수정 정보", required = true) @Valid @RequestBody
                    FeatureUpdateRequest request) {
        FeatureResponse response = featureService.updateFeature(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "피처 회전 수정", description = "ID로 피처의 회전 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "회전 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "피처를 찾을 수 없음",
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
    @PatchMapping("/{id}/rotation")
    public ResponseEntity<FeatureResponse> updateRotation(
            @Parameter(description = "피처 ID", required = true) @PathVariable String id,
            @Parameter(description = "회전 수정 정보", required = true) @Valid @RequestBody
                    FeatureUpdateRequest request) {
        FeatureResponse response = featureService.updateFeature(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "피처 크기 수정", description = "ID로 피처의 크기 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "크기 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "피처를 찾을 수 없음",
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
    @PatchMapping("/{id}/scale")
    public ResponseEntity<FeatureResponse> updateScale(
            @Parameter(description = "피처 ID", required = true) @PathVariable String id,
            @Parameter(description = "크기 수정 정보", required = true) @Valid @RequestBody
                    FeatureUpdateRequest request) {
        FeatureResponse response = featureService.updateFeature(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "피처 삭제", description = "ID로 피처를 삭제합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "피처 삭제 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "피처를 찾을 수 없음",
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
    public ResponseEntity<Void> deleteFeature(
            @Parameter(description = "피처 ID", required = true) @PathVariable String id) {
        featureService.deleteFeature(id);
        return ResponseEntity.noContent().build();
    }

    // Feature에 Asset 할당 API
    @Operation(summary = "피처에 에셋 할당", description = "특정 피처에 에셋을 할당(연결)합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "에셋 할당 성공", content = @Content(schema = @Schema(implementation = FeatureResponse.class))),
                @ApiResponse(responseCode = "404", description = "피처 또는 에셋을 찾을 수 없음")
            })
    @PutMapping("/{featureId}/assets/{assetId}")
    public ResponseEntity<FeatureResponse> assignAssetToFeature(
            @Parameter(description = "피처 ID (UUID)", required = true) @PathVariable String featureId,
            @Parameter(description = "에셋 ID (Long)", required = true) @PathVariable Long assetId) {
        FeatureResponse response = featureService.assignAssetToFeature(featureId, assetId);
        return ResponseEntity.ok(response);
    }

    // Feature에서 Asset 연결 해제 API
    @Operation(summary = "피처에서 에셋 연결 해제", description = "특정 피처에 할당된 에셋과의 연결을 해제합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "에셋 연결 해제 성공", content = @Content(schema = @Schema(implementation = FeatureResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 피처에 에셋이 할당되지 않음)"),
                @ApiResponse(responseCode = "404", description = "피처를 찾을 수 없음")
            })
    @DeleteMapping("/{featureId}/assets")
    public ResponseEntity<FeatureResponse> removeAssetFromFeature(
            @Parameter(description = "피처 ID (UUID)", required = true) @PathVariable String featureId) {
        FeatureResponse response = featureService.removeAssetFromFeature(featureId);
        return ResponseEntity.ok(response);
    }
}
