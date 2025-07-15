package com.pluxity.user.controller;

import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ErrorResponseBody;
import com.pluxity.user.dto.*;
import com.pluxity.user.service.UserService;
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
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Controller", description = "관리자용 사용자 관리 API")
public class AdminUserController {

    private final UserService service;

    @Operation(summary = "사용자 목록 조회", description = "모든 사용자 목록을 조회합니다")
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
                        responseCode = "403",
                        description = "권한 없음",
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
    public ResponseEntity<DataResponseBody<List<UserResponse>>> getUsers() {
        return ResponseEntity.ok(DataResponseBody.of(service.findAll()));
    }

    @Operation(summary = "사용자 상세 조회", description = "ID로 특정 사용자의 상세 정보를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "사용자 조회 성공"),
                @ApiResponse(
                        responseCode = "401",
                        description = "인증되지 않은 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "403",
                        description = "권한 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "사용자를 찾을 수 없음",
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
    @GetMapping(value = "/{id}")
    public ResponseEntity<DataResponseBody<UserResponse>> getUser(
            @Parameter(description = "사용자 ID", required = true) @PathVariable("id") Long id) {
        return ResponseEntity.ok(DataResponseBody.of(service.findById(id)));
    }

    @Operation(summary = "로그인된 사용자 정보 조회", description = "현재 로그인된 사용자의 정보를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "로그인된 사용자 정보 조회 성공"),
                @ApiResponse(
                        responseCode = "401",
                        description = "인증되지 않은 요청 (예: 세션이 없거나 유효하지 않은 토큰)",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "403",
                        description = "권한 없음 (예: 인증은 되었으나 해당 정보 접근 권한이 없는 경우)",
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
    @GetMapping("/with-is-logged-in")
    public ResponseEntity<DataResponseBody<List<UserLoggedInResponse>>> getLoggedInUser() {
        return ResponseEntity.ok(DataResponseBody.of(service.isLoggedIn())); // 실제 서비스 호출
    }

    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "사용자 생성 성공"),
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
                        responseCode = "403",
                        description = "권한 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "409",
                        description = "이미 존재하는 사용자",
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
    @ResponseCreated(path = "/admin/users/{id}")
    public ResponseEntity<Long> saveUser(
            @Parameter(description = "사용자 생성 정보", required = true) @Valid @RequestBody
                    UserCreateRequest request) {
        return ResponseEntity.ok(service.save(request).id());
    }

    @Operation(summary = "사용자에게 역할 할당", description = "특정 사용자에게 역할을 할당합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "역할 할당 성공"),
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
                        responseCode = "403",
                        description = "권한 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "사용자 또는 역할을 찾을 수 없음",
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
    @PostMapping("/{userId}/roles")
    @ResponseCreated(path = "/admin/roles/{id}")
    public ResponseEntity<Long> assignRolesToUser(
            @Parameter(description = "사용자 ID", required = true) @PathVariable("userId") Long userId,
            @Parameter(description = "할당할 역할 정보", required = true) @RequestBody @Valid
                    UserRoleAssignRequest request) {
        return ResponseEntity.ok(service.assignRolesToUser(userId, request).id());
    }

    @Operation(summary = "사용자 정보 수정", description = "기존 사용자의 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "사용자 수정 성공"),
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
                        responseCode = "403",
                        description = "권한 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "사용자를 찾을 수 없음",
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
    @PatchMapping(value = "/{id}")
    public ResponseEntity<Void> updateUser(
            @Parameter(description = "사용자 ID", required = true) @PathVariable("id") Long id,
            @Parameter(description = "사용자 수정 정보", required = true) @RequestBody @Valid
                    UserUpdateRequest dto) {
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사용자 비밀번호 변경", description = "사용자의 비밀번호를 변경합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "비밀번호 변경 성공"),
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
                        responseCode = "403",
                        description = "권한 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "사용자를 찾을 수 없음",
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
    @PatchMapping(value = "/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @Parameter(description = "사용자 ID", required = true) @PathVariable("id") Long id,
            @Parameter(description = "비밀번호 변경 정보", required = true) @Valid @RequestBody
                    UserPasswordUpdateRequest dto) {
        service.updateUserPassword(id, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사용자 역할 수정", description = "사용자의 역할을 수정합니다")
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
                        responseCode = "403",
                        description = "권한 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "사용자를 찾을 수 없음",
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
    @PutMapping(value = "/{id}/roles")
    public ResponseEntity<Void> updateRoles(
            @Parameter(description = "사용자 ID", required = true) @PathVariable("id") Long id,
            @Parameter(description = "역할 수정 정보", required = true) @Valid @RequestBody
                    UserRoleUpdateRequest dto) {
        service.updateUserRoles(id, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사용자 삭제", description = "ID로 사용자를 삭제합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
                @ApiResponse(
                        responseCode = "401",
                        description = "인증되지 않은 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "403",
                        description = "권한 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "사용자를 찾을 수 없음",
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
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "사용자 ID", required = true) @PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사용자에서 역할 제거", description = "특정 사용자에서 역할을 제거합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "역할 제거 성공"),
                @ApiResponse(
                        responseCode = "401",
                        description = "인증되지 않은 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "403",
                        description = "권한 없음",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "사용자 또는 역할을 찾을 수 없음",
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
    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<Void> removeRoleFromUser(
            @Parameter(description = "사용자 ID", required = true) @PathVariable("userId") Long userId,
            @Parameter(description = "역할 ID", required = true) @PathVariable("roleId") Long roleId) {
        service.removeRoleFromUser(userId, roleId);
        return ResponseEntity.noContent().build();
    }
}
