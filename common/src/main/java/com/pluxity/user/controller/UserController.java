package com.pluxity.user.controller;

import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ErrorResponseBody;
import com.pluxity.global.response.ResponseBody;
import com.pluxity.user.dto.UserResponse;
import com.pluxity.user.dto.UserUpdateRequest;
import com.pluxity.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "사용자 정보 관리 API")
public class UserController {

    private final UserService service;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<DataResponseBody<UserResponse>> getUser(Authentication authentication) {
        var username = authentication.getName();
        return ResponseEntity.ok(DataResponseBody.of(service.findByUsername(username)));
    }

    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class)))
    })
    @PutMapping("/me")
    public ResponseEntity<Void> updateUser(
            Authentication authentication, 
            @Parameter(description = "사용자 수정 정보", required = true)
            @RequestBody UserUpdateRequest dto) {
        Long id = service.findByUsername(authentication.getName()).id();
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }
}
