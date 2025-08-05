package com.pluxity.cctv.category;

import com.pluxity.cctv.category.dto.CctvCategoryAllResponse;
import com.pluxity.cctv.category.dto.CctvCategoryCreateRequest;
import com.pluxity.cctv.category.dto.CctvCategoryUpdateRequest;
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

@RequestMapping("/cctv-categories")
@RestController
@RequiredArgsConstructor
@Tag(name = "Cctv Category Controller", description = "Cctv 카테고리 관리 API")
public class CctvCategoryController {

    private final CctvCategoryService cctvCategoryService;

    @Operation(summary = "CCTV 카테고리 생성", description = "새로운 CCTV 카테고리를 생성합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "CCTV 카테고리 생성 성공",
                        content = @Content(mediaType = "application/json")),
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
    @ResponseCreated(path = "/cctv-categories/{id}")
    public ResponseEntity<Long> createCctvCategory(
            @Parameter(description = "CCTV 카테고리 생성 정보", required = true) @Valid @RequestBody
                    CctvCategoryCreateRequest req) {
        return ResponseEntity.ok(cctvCategoryService.create(req));
    }

    @Operation(summary = "CCTV 카테고리 목록 조회", description = "모든 CCTV 카테고리 목록을 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "CCTV 카테고리 목록 조회 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "파라미터 오류",
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
    @GetMapping
    public ResponseEntity<DataResponseBody<CctvCategoryAllResponse>> getCctvCategories() {
        return ResponseEntity.ok(DataResponseBody.of(cctvCategoryService.findAll()));
    }

    @Operation(summary = "CCTV 카테고리 수정", description = "ID를 기반으로 CCTV 카테고리를 수정합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "CCTV 카테고리 수정 성공"),
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
    public ResponseEntity<Void> patchFacilityCategory(
            @Parameter(description = "CCTV 카테고리 ID", required = true) @PathVariable Long id,
            @Parameter(description = "CCTV 카테고리 수정 정보", required = true) @Valid @RequestBody
                    CctvCategoryUpdateRequest request) {
        cctvCategoryService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "CCTV 카테고리 삭제", description = "ID를 기반으로 CCTV 카테고리를 삭제합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "CCTV 카테고리 삭제 성공"),
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
            @Parameter(description = "CCTV 카테고리 ID", required = true) @PathVariable Long id) {
        cctvCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
