package com.pluxity.asset.controller;

import com.pluxity.asset.dto.AssetCreateRequest;
import com.pluxity.asset.dto.AssetResponse;
import com.pluxity.asset.dto.AssetUpdateRequest;
import com.pluxity.asset.service.AssetService;
import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.global.response.ErrorResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/assets")
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Asset Controller", description = "에셋 관리 API")
public class AssetController {

    private final AssetService service;

    @Operation(summary = "에셋 목록 조회", description = "모든 에셋 목록을 조회합니다")
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
    public ResponseEntity<List<AssetResponse>> getAssets() {
        return ResponseEntity.ok(service.getAssets());
    }

    @Operation(summary = "에셋 상세 조회", description = "ID로 특정 에셋의 상세 정보를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "에셋 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "에셋을 찾을 수 없음",
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
    public ResponseEntity<AssetResponse> getAsset(
            @Parameter(description = "에셋 ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(service.getAsset(id));
    }

    @Operation(summary = "에셋 생성", description = "새로운 에셋을 생성합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "에셋 생성 성공"),
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
    public ResponseEntity<Long> createAsset(
            @Parameter(description = "에셋 생성 정보", required = true) @RequestBody
                    AssetCreateRequest request) {
        return ResponseEntity.ok(service.createAsset(request));
    }

    @Operation(summary = "에셋 수정", description = "기존 에셋의 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "에셋 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "에셋을 찾을 수 없음",
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
    public ResponseEntity<Void> updateAsset(
            @Parameter(description = "에셋 ID", required = true) @PathVariable Long id,
            @Parameter(description = "에셋 수정 정보", required = true) @RequestBody
                    AssetUpdateRequest request) {
        service.updateAsset(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "에셋 삭제", description = "ID로 에셋을 삭제합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "에셋 삭제 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "에셋을 찾을 수 없음",
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
    public ResponseEntity<Void> deleteAsset(
            @Parameter(description = "에셋 ID", required = true) @PathVariable Long id) {
        service.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }
}
