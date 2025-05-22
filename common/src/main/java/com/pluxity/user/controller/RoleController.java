package com.pluxity.user.controller;

import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ErrorResponseBody;
import com.pluxity.user.dto.*;
import com.pluxity.user.service.RoleService;
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
@RequestMapping("/roles")
@RequiredArgsConstructor
@Tag(name = "Role Controller", description = "역할 관리 API")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "역할 상세 조회", description = "ID로 특정 역할의 상세 정보를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "역할 조회 성공"),
                @ApiResponse(
                        responseCode = "401",
                        description = "인증되지 않은 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "역할을 찾을 수 없음",
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
    public ResponseEntity<DataResponseBody<RoleResponse>> getRole(
            @Parameter(description = "역할 ID", required = true) @PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(DataResponseBody.of(roleService.findById(id)));
    }

    @Operation(summary = "역할 목록 조회", description = "모든 역할 목록을 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
                @ApiResponse(
                        responseCode = "401",
                        description = "인증되지 않은 요청",
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
    public ResponseEntity<DataResponseBody<List<RoleResponse>>> getAllRoles() {
        return ResponseEntity.ok(DataResponseBody.of(roleService.findAll()));
    }

    @Operation(summary = "역할 생성", description = "새로운 역할을 생성합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "역할 생성 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "401",
                        description = "인증되지 않은 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "409",
                        description = "이미 존재하는 역할명",
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
    @ResponseCreated(path = "/roles/{id}")
    public ResponseEntity<Long> createRole(
            @Parameter(description = "역할 생성 정보", required = true) @Valid @RequestBody
                    RoleCreateRequest request) {
        return ResponseEntity.ok(roleService.save(request).id());
    }

    @Operation(summary = "역할 수정", description = "기존 역할의 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "역할 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "401",
                        description = "인증되지 않은 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "역할을 찾을 수 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "409",
                        description = "이미 존재하는 역할명",
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
    public ResponseEntity<Void> updateRole(
            @Parameter(description = "역할 ID", required = true) @PathVariable(name = "id") Long id,
            @Parameter(description = "역할 수정 정보", required = true) @Valid @RequestBody
                    RoleUpdateRequest request) {
        roleService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "역할 삭제", description = "ID로 역할을 삭제합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "역할 삭제 성공"),
                @ApiResponse(
                        responseCode = "401",
                        description = "인증되지 않은 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "역할을 찾을 수 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "409",
                        description = "사용 중인 역할 삭제 시도",
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
    public ResponseEntity<Void> deleteRole(
            @Parameter(description = "역할 ID", required = true) @PathVariable(name = "id") Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
