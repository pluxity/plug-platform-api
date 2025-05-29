package com.pluxity.device.controller;

import com.pluxity.device.dto.DeviceCategoryRequest;
import com.pluxity.device.dto.DeviceCategoryResponse;
import com.pluxity.device.dto.DeviceCategoryTreeResponse;
import com.pluxity.device.service.DeviceCategoryService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@RestController
//@RequestMapping("/device-categories")
@RequiredArgsConstructor
@Tag(name = "Device Category Controller", description = "디바이스 카테고리 관리 API")
public class DeviceCategoryController {

    private final DeviceCategoryService deviceCategoryService;

    @Operation(summary = "디바이스 카테고리 생성", description = "새로운 디바이스 카테고리를 생성합니다")
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
    @ResponseCreated(path = "/device-categories/{id}")
    public ResponseEntity<Long> create(
            @Parameter(description = "카테고리 생성 정보", required = true) @Valid @RequestBody
                    DeviceCategoryRequest request) {
        Long id = deviceCategoryService.create(request);
        return ResponseEntity.ok(id);
    }

    @Operation(summary = "루트 카테고리 목록 조회", description = "모든 최상위 디바이스 카테고리 목록을 조회합니다")
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
    public ResponseEntity<DataResponseBody<List<DeviceCategoryResponse>>> getRootCategories() {
        List<DeviceCategoryResponse> responses = deviceCategoryService.getRootDeviceCategoryResponses();
        return ResponseEntity.ok(DataResponseBody.of(responses));
    }

    @Operation(summary = "카테고리 트리 조회", description = "카테고리의 계층 구조를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "조회 성공"),
                @ApiResponse(
                        responseCode = "500",
                        description = "서버 오류",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @GetMapping("/tree")
    public ResponseEntity<DataResponseBody<List<DeviceCategoryTreeResponse>>> getCategoryTree() {
        List<DeviceCategoryTreeResponse> responses = deviceCategoryService.getDeviceCategoryTree();
        return ResponseEntity.ok(DataResponseBody.of(responses));
    }

    @Operation(summary = "카테고리 상세 조회", description = "ID로 특정 카테고리의 상세 정보를 조회합니다")
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
    public ResponseEntity<DataResponseBody<DeviceCategoryResponse>> getCategory(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long id) {
        DeviceCategoryResponse response = deviceCategoryService.getDeviceCategoryResponse(id);
        return ResponseEntity.ok(DataResponseBody.of(response));
    }

    @Operation(summary = "하위 카테고리 조회", description = "특정 ID를 가진 카테고리의 하위 카테고리 목록을 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "조회 성공"),
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
    @GetMapping("/{id}/children")
    public ResponseEntity<DataResponseBody<List<DeviceCategoryResponse>>> getChildren(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long id) {
        List<DeviceCategoryResponse> responses = deviceCategoryService.getChildrenResponses(id);
        return ResponseEntity.ok(DataResponseBody.of(responses));
    }

    @Operation(summary = "카테고리 수정", description = "기존 카테고리의 정보를 수정합니다")
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
    public ResponseEntity<Void> update(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long id,
            @Parameter(description = "카테고리 수정 정보", required = true) @Valid @RequestBody
                    DeviceCategoryRequest request) {
        deviceCategoryService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "카테고리 삭제", description = "ID로 카테고리를 삭제합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "카테고리 삭제 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "카테고리에 디바이스가 등록되어 있어 삭제할 수 없음",
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
    public ResponseEntity<Void> delete(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long id) {
        deviceCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
