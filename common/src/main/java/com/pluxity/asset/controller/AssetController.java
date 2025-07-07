package com.pluxity.asset.controller;

import com.pluxity.asset.dto.AssetCreateRequest;
import com.pluxity.asset.dto.AssetResponse;
import com.pluxity.asset.dto.AssetUpdateRequest;
import com.pluxity.asset.service.AssetService;
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
    public ResponseEntity<DataResponseBody<List<AssetResponse>>> getAssets() {
        return ResponseEntity.ok(DataResponseBody.of(service.getAssets()));
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
    public ResponseEntity<DataResponseBody<AssetResponse>> getAsset(
            @Parameter(description = "에셋 ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(DataResponseBody.of(service.getAsset(id)));
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
    @ResponseCreated(path = "/assets/{id}")
    public ResponseEntity<Long> createAsset(
            @Parameter(description = "에셋 생성 정보", required = true) @RequestBody @Valid
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
            @Parameter(description = "에셋 수정 정보", required = true) @RequestBody @Valid
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

    @Operation(summary = "에셋에 카테고리 할당", description = "에셋에 특정 카테고리를 할당합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "카테고리 할당 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "에셋 또는 카테고리를 찾을 수 없음",
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
    @PutMapping("/{id}/category/{categoryId}")
    public ResponseEntity<Void> assignCategory(
            @Parameter(description = "에셋 ID", required = true) @PathVariable Long id,
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long categoryId) {
        service.assignCategory(id, categoryId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "에셋에서 카테고리 제거", description = "에셋에서 카테고리 할당을 제거합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "카테고리 제거 성공"),
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
    @DeleteMapping("/{id}/category")
    public ResponseEntity<Void> removeCategory(
            @Parameter(description = "에셋 ID", required = true) @PathVariable Long id) {
        service.removeCategory(id);
        return ResponseEntity.noContent().build();
    }
}
