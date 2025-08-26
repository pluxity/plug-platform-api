package com.pluxity.domains.device.controller;

import com.pluxity.domains.device.dto.NfluxCategoryAllResponse;
import com.pluxity.domains.device.dto.NfluxCategoryCreateRequest;
import com.pluxity.domains.device.dto.NfluxCategoryResponse;
import com.pluxity.domains.device.dto.NfluxCategoryUpdateRequest;
import com.pluxity.domains.device.service.NfluxCategoryService;
import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ErrorResponseBody;
import com.pluxity.global.response.ResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/device-categories")
@RequiredArgsConstructor
@Tag(name = "Nflux Category Controller", description = "엔플럭스 카테고리 관리 API")
public class NfluxCategoryController {

    private final NfluxCategoryService nfluxCategoryService;

    @Operation(summary = "엔플럭스 카테고리 생성", description = "새로운 엔플럭스 카테고리를 생성합니다")
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
    @ResponseStatus(HttpStatus.CREATED)
    public DataResponseBody<Long> create(
            @Parameter(description = "엔플럭스 카테고리 생성 정보", required = true) @Valid @RequestBody
                    NfluxCategoryCreateRequest request) {
        Long id = nfluxCategoryService.save(request);
        return DataResponseBody.of(HttpStatus.CREATED, "엔플럭스 카테고리가 생성되었습니다.", id);
    }

    @Operation(summary = "엔플럭스 카테고리 목록 조회", description = "모든 엔플럭스 카테고리 목록을 조회합니다")
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
    public DataResponseBody<NfluxCategoryAllResponse> findAll() {
        NfluxCategoryAllResponse categories = nfluxCategoryService.getNfluxCategories();
        return DataResponseBody.of(categories);
    }

    @Operation(summary = "하위 엔플럭스 카테고리 목록 조회", description = "특정 카테고리의 직계 하위 카테고리 목록을 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "부모 카테고리를 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "500",
                        description = "서버 오류",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @GetMapping("/{id}/children")
    public ResponseEntity<DataResponseBody<List<NfluxCategoryResponse>>> findAllRoots(
            @Parameter(description = "부모 카테고리 ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(
                DataResponseBody.of(nfluxCategoryService.getChildDeviceCategories(id)));
    }

    @Operation(summary = "엔플럭스 카테고리 수정", description = "기존 엔플럭스 카테고리의 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "카테고리 수정 성공"),
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
                    NfluxCategoryUpdateRequest request) {
        nfluxCategoryService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Nflux 카테고리 삭제", description = "ID로 Nflux 카테고리를 삭제합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "카테고리 삭제 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "카테고리를 찾을 수 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "하위 카테고리나 연결된 엔플럭스가 있어 삭제할 수 없음",
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
    public ResponseBody delete(
            @Parameter(description = "카테고리 ID", required = true) @PathVariable Long id) {
        nfluxCategoryService.delete(id);
        return ResponseBody.of(HttpStatus.NO_CONTENT, "NFlux 카테고리가 삭제되었습니다.");
    }
}
