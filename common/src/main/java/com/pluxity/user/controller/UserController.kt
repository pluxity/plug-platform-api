package com.pluxity.user.controller

import com.pluxity.global.response.DataResponseBody
import com.pluxity.global.response.ErrorResponseBody
import com.pluxity.user.dto.UserPasswordUpdateRequest
import com.pluxity.user.dto.UserResponse
import com.pluxity.user.dto.UserUpdateRequest
import com.pluxity.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
@Tag(name = "User Controller", description = "사용자 정보 관리 API")
class UserController(
    private val service: UserService,
) {
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "정보 조회 성공",
            ), ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ), ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
        ],
    )
    @GetMapping("/me")
    fun getUser(authentication: Authentication): ResponseEntity<DataResponseBody<UserResponse>> =
        ResponseEntity.ok(
            DataResponseBody.of(
                service.findByUsername(authentication.name),
            ),
        )

    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "정보 수정 성공",
            ), ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ), ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ), ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
        ],
    )
    @PatchMapping("/me")
    fun updateUser(
        authentication: Authentication,
        @Parameter(description = "사용자 수정 정보", required = true) @RequestBody @Valid dto: UserUpdateRequest,
    ): ResponseEntity<Void> {
        val id = service.findByUsername(authentication.name).id
        service.update(id, dto)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "사용자 비밀번호 변경", description = "사용자의 비밀번호를 변경합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "비밀번호 변경 성공",
            ), ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ), ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 요청",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ), ApiResponse(
                responseCode = "403",
                description = "권한 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ), ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ), ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
        ],
    )
    @PatchMapping(value = ["/me/password"])
    fun updatePassword(
        authentication: Authentication,
        @Parameter(description = "비밀번호 변경 정보", required = true) @RequestBody @Valid dto: UserPasswordUpdateRequest,
    ): ResponseEntity<Void> {
        service.updateUserPassword(authentication.name, dto)
        return ResponseEntity.noContent().build()
    }
}
