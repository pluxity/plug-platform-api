package com.pluxity.icon.controller;

import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ErrorResponseBody;
import com.pluxity.icon.dto.IconCreateRequest;
import com.pluxity.icon.dto.IconResponse;
import com.pluxity.icon.dto.IconUpdateRequest;
import com.pluxity.icon.service.IconService;
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

@RestController
@RequestMapping("/icons")
@RequiredArgsConstructor
@Tag(name = "Icon Controller", description = "아이콘 관리 API")
public class IconController {

    private final IconService service;

    @Operation(summary = "아이콘 생성", description = "새로운 아이콘을 생성합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "아이콘 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class)))
    })
    @PostMapping
    @ResponseCreated
    public ResponseEntity<Long> create(
            @Parameter(description = "아이콘 생성 정보", required = true)
            @Valid @RequestBody IconCreateRequest request) {
        Long id = service.createIcon(request);
        return ResponseEntity.ok(id);
    }

    @Operation(summary = "아이콘 목록 조회", description = "모든 아이콘 목록을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class)))
    })
    @GetMapping
    public ResponseEntity<DataResponseBody<List<IconResponse>>> getAll() {
        return ResponseEntity.ok(DataResponseBody.of(service.getIcons()));
    }

    @Operation(summary = "아이콘 상세 조회", description = "ID로 특정 아이콘의 상세 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "아이콘 조회 성공"),
            @ApiResponse(responseCode = "404", description = "아이콘을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<DataResponseBody<IconResponse>> get(
            @Parameter(description = "아이콘 ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(DataResponseBody.of(service.getIcon(id)));
    }

    @Operation(summary = "아이콘 수정", description = "기존 아이콘의 정보를 수정합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "아이콘 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class))),
            @ApiResponse(responseCode = "404", description = "아이콘을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class)))
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Void> patch(
            @Parameter(description = "아이콘 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "아이콘 수정 정보", required = true)
            @Valid @RequestBody IconUpdateRequest request) {
        service.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "아이콘 삭제", description = "ID로 아이콘을 삭제합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "아이콘 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "아이콘을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "아이콘 ID", required = true)
            @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
} 