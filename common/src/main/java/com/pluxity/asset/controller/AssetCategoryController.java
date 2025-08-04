package com.pluxity.asset.controller;

import com.pluxity.asset.dto.AssetCategoryAllResponse;
import com.pluxity.asset.dto.AssetCategoryCreateRequest;
import com.pluxity.asset.dto.AssetCategoryResponse;
import com.pluxity.asset.dto.AssetCategoryUpdateRequest;
import com.pluxity.asset.service.AssetCategoryService;
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

@RequestMapping("/asset-categories")
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Asset Category Controller", description = "에셋 카테고리 관리 API")
public class AssetCategoryController {

    private final AssetCategoryService service;

    @Operation(summary = "에셋 카테고리 목록 조회", description = "모든 에셋 카테고리 목록을 조회합니다")
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
    public ResponseEntity<DataResponseBody<AssetCategoryAllResponse>> getAllCategories() {
        return ResponseEntity.ok(DataResponseBody.of(service.getAllCategories()));
    }

    @Operation(summary = "하위 에셋 카테고리 목록 조회", description = "특정 카테고리의 하위 카테고리 목록을 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "부모 카테고리를 찾을 수 없음",
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
    @GetMapping("/{id}/children")
    public ResponseEntity<DataResponseBody<List<AssetCategoryResponse>>> getChildCategories(
            @Parameter(description = "부모 카테고리 ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(DataResponseBody.of(service.getChildCategories(id)));
    }

    @Operation(summary = "에셋 카테고리 상세 조회", description = "ID로 특정 에셋 카테고리의 상세 정보를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "카테고리 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "카테고리를 찾을 수 없음",
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
    public ResponseEntity<DataResponseBody<AssetCategoryResponse>> getAssetCategory(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(DataResponseBody.of(service.getAssetCategory(id)));
    }

    @Operation(summary = "에셋 카테고리 생성", description = "새로운 에셋 카테고리를 생성합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "카테고리 생성 성공"),
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
    @ResponseCreated(path = "/asset-categories/{id}")
    public ResponseEntity<Long> createAssetCategory(
            @Parameter(description = "카테고리 생성 정보", required = true) @RequestBody @Valid
                    AssetCategoryCreateRequest request) {
        return ResponseEntity.ok(service.createAssetCategory(request));
    }

    @Operation(summary = "에셋 카테고리 수정", description = "기존 에셋 카테고리의 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "카테고리 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "카테고리를 찾을 수 없음",
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
    public ResponseEntity<Void> updateAssetCategory(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long id,
            @Parameter(description = "카테고리 수정 정보", required = true) @RequestBody @Valid
                    AssetCategoryUpdateRequest request) {
        service.updateAssetCategory(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "에셋 카테고리 삭제", description = "ID로 에셋 카테고리를 삭제합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "카테고리 삭제 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "삭제할 수 없는 카테고리",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "카테고리를 찾을 수 없음",
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
    public ResponseEntity<Void> deleteAssetCategory(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long id) {
        service.deleteAssetCategory(id);
        return ResponseEntity.noContent().build();
    }
}
