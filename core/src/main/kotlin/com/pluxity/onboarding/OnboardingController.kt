package com.pluxity.onboarding

import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.global.response.ErrorResponseBody
import com.pluxity.onboarding.dto.AdminUserCreateRequest
import com.pluxity.onboarding.dto.OnboardingFileResponse
import com.pluxity.onboarding.service.OnboardingFileService
import com.pluxity.onboarding.service.OnboardingUserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/onboarding")
@Tag(name = "Onboarding Controller", description = "온보딩 API")
class OnboardingController(
    private val userService: OnboardingUserService,
    private val fileService: OnboardingFileService,
) {
    @Operation(summary = "관리자 사용자 생성", description = "새로운 관리자 계정을 생성합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "관리자 사용자 생성 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (validation 실패)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "409",
                description = "이미 존재하는 사용자명",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
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
    @PostMapping("/users")
    @ResponseCreated(path = "/api/v1/onboarding/users/{id}")
    fun createAdminUser(
        @Parameter(description = "관리자 사용자 생성 정보", required = true)
        @RequestBody @Valid request: AdminUserCreateRequest
    ): ResponseEntity<Long> = ResponseEntity.ok(userService.createAdminUser(request))

    @Operation(
        summary = "파일 업로드",
        description = "파일을 임시 저장소에 업로드하고 File ID와 URL을 반환합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "파일 업로드 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = OnboardingFileResponse::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 오류",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ]
            )
        ]
    )
    @PostMapping("/files", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
        @Parameter(description = "업로드할 파일", required = true)
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<OnboardingFileResponse> = ResponseEntity.ok(fileService.uploadFile(file))
}