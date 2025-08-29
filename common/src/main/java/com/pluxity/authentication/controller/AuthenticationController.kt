package com.pluxity.authentication.controller

import com.pluxity.authentication.dto.SignInRequest
import com.pluxity.authentication.dto.SignUpRequest
import com.pluxity.authentication.service.AuthenticationService
import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.global.response.ErrorResponseBody
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication Controller", description = "사용자 인증 API")
class AuthenticationController(
    private val authenticationService: AuthenticationService,
) {
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "회원가입 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "409",
                description = "이미 존재하는 사용자",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @ResponseCreated(path = "/users/me")
    @PostMapping("/sign-up")
    fun signUp(
        @Parameter(description = "회원가입 정보", required = true) @RequestBody @Valid dto: SignUpRequest,
    ): ResponseEntity<Long> = ResponseEntity.ok(authenticationService.signUp(dto))

    @Operation(summary = "로그인", description = "사용자 인증 및 세션 생성")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "로그인 성공"),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PostMapping("/sign-in", produces = ["application/json"])
    @ResponseCreated(path = "/users/me")
    fun signIn(
        @Parameter(description = "로그인 정보", required = true) @RequestBody @Valid signInRequestDto: SignInRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        authenticationService.signIn(signInRequestDto, request, response)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "로그아웃", description = "사용자 세션 종료")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 요청",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PostMapping("/sign-out", produces = ["application/json"])
    fun signOut(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        authenticationService.signOut(request, response)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "토큰 갱신", description = "인증 토큰 갱신")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "토큰 갱신 성공"),
            ApiResponse(
                responseCode = "401",
                description = "유효하지 않은 리프레시 토큰",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PostMapping("/refresh-token")
    fun refreshToken(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        authenticationService.refreshToken(request, response)
        return ResponseEntity.noContent().build()
    }
}
