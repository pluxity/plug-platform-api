package com.pluxity.user.controller

import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.global.response.DataResponseBody
import com.pluxity.global.response.ErrorResponseBody
import com.pluxity.user.dto.UserCreateRequest
import com.pluxity.user.dto.UserLoggedInResponse
import com.pluxity.user.dto.UserPasswordUpdateRequest
import com.pluxity.user.dto.UserResponse
import com.pluxity.user.dto.UserRoleUpdateRequest
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/users")
@Tag(name = "Admin User Controller", description = "관리자용 사용자 관리 API")
class AdminUserController(
    private val service: UserService,
) {
    @GetMapping
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "목록 조회 성공",
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
    @Operation(summary = "사용자 목록 조회", description = "모든 사용자 목록을 조회합니다")
    fun getUsers(): ResponseEntity<DataResponseBody<List<UserResponse>>> = ResponseEntity.ok(DataResponseBody.of(service.findAll()))

    @Operation(summary = "사용자 상세 조회", description = "ID로 특정 사용자의 상세 정보를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "사용자 조회 성공",
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
    @GetMapping(value = ["/{id}"])
    fun getUser(
        @Parameter(description = "사용자 ID", required = true) @PathVariable("id") id: Long,
    ): ResponseEntity<DataResponseBody<UserResponse>> = ResponseEntity.ok(DataResponseBody.of(service.findById(id)))

    @GetMapping("/with-is-logged-in")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "로그인된 사용자 정보 조회 성공",
            ), ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 요청 (예: 세션이 없거나 유효하지 않은 토큰)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ), ApiResponse(
                responseCode = "403",
                description = "권한 없음 (예: 인증은 되었으나 해당 정보 접근 권한이 없는 경우)",
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
    @Operation(summary = "로그인된 사용자 정보 조회", description = "현재 로그인된 사용자의 정보를 조회합니다.")
    fun getLoggedInUser(): ResponseEntity<DataResponseBody<List<UserLoggedInResponse>>> =
        ResponseEntity.ok(DataResponseBody.of(service.isLoggedIn()))

    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "사용자 생성 성공",
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
                responseCode = "409",
                description = "이미 존재하는 사용자",
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
    @PostMapping
    @ResponseCreated(path = "/admin/users/{id}")
    fun saveUser(
        @Parameter(description = "사용자 생성 정보", required = true) @RequestBody @Valid request: UserCreateRequest,
    ): ResponseEntity<Long> = ResponseEntity.ok(service.save(request).id)

    @Operation(summary = "사용자 정보 수정", description = "기존 사용자의 정보를 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "사용자 수정 성공",
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
    @PatchMapping(value = ["/{id}"])
    fun updateUser(
        @Parameter(description = "사용자 ID", required = true) @PathVariable("id") id: Long,
        @Parameter(description = "사용자 수정 정보", required = true) @RequestBody @Valid dto: UserUpdateRequest,
    ): ResponseEntity<Void> {
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
    @PatchMapping(value = ["/{id}/password"])
    fun updatePassword(
        @Parameter(description = "사용자 ID", required = true) @PathVariable("id") id: Long,
        @Parameter(description = "비밀번호 변경 정보", required = true) @Valid @RequestBody dto: UserPasswordUpdateRequest,
    ): ResponseEntity<Void> {
        service.updateUserPassword(id, dto)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "사용자 역할 수정", description = "사용자의 역할을 수정합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "역할 수정 성공",
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
    @PatchMapping(value = ["/{id}/roles"])
    fun updateRoles(
        @Parameter(description = "사용자 ID", required = true) @PathVariable("id") id: Long,
        @Parameter(description = "역할 수정 정보", required = true) @Valid @RequestBody dto: UserRoleUpdateRequest,
    ): ResponseEntity<Void> {
        service.updateUserRoles(id, dto)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "사용자 삭제", description = "ID로 사용자를 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "사용자 삭제 성공",
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
    @DeleteMapping(value = ["/{id}"])
    fun deleteUser(
        @Parameter(description = "사용자 ID", required = true) @PathVariable("id") id: Long,
    ): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "사용자에서 역할 제거", description = "특정 사용자에서 역할을 제거합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "역할 제거 성공",
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
                description = "사용자 또는 역할을 찾을 수 없음",
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
    @DeleteMapping("/{userId}/roles/{roleId}")
    fun removeRoleFromUser(
        @Parameter(description = "사용자 ID", required = true) @PathVariable("userId") userId: Long,
        @Parameter(description = "역할 ID", required = true) @PathVariable("roleId") roleId: Long,
    ): ResponseEntity<Void> {
        service.removeRoleFromUser(userId, roleId)
        return ResponseEntity.noContent().build<Void>()
    }

    @Operation(summary = "사용자 비밀번호 초기화", description = "사용자의 비밀번호를 초기화합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "비밀번호 초기화 성공",
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
    @PatchMapping(value = ["/{id}/password-init"])
    fun initPassword(
        @Parameter(description = "사용자 ID", required = true) @PathVariable("id") id: Long,
    ): ResponseEntity<Void> {
        service.initPassword(id)
        return ResponseEntity.noContent().build<Void>()
    }
}
