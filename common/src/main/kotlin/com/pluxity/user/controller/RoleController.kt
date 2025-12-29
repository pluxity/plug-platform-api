package com.pluxity.user.controller

import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.global.response.DataResponseBody
import com.pluxity.global.response.ErrorResponseBody
import com.pluxity.user.dto.RoleCreateRequest
import com.pluxity.user.dto.RoleResponse
import com.pluxity.user.dto.RoleUpdateRequest
import com.pluxity.user.service.RoleService
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/roles")
@Tag(name = "Role Controller", description = "역할 관리 API")
class RoleController(
    private val roleService: RoleService,
) {
    @Operation(summary = "역할 상세 조회", description = "ID로 특정 역할의 상세 정보를 조회합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "역할 조회 성공",
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
                responseCode = "404",
                description = "역할을 찾을 수 없음",
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
    @GetMapping("/{id}")
    fun getRole(
        @PathVariable @Parameter(description = "역할 ID", required = true) id: Long,
    ): ResponseEntity<DataResponseBody<RoleResponse>> = ResponseEntity.ok(DataResponseBody(roleService.findById(id)))

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
    @Operation(summary = "역할 목록 조회", description = "모든 역할 목록을 조회합니다")
    fun getAllRoles(): ResponseEntity<DataResponseBody<List<RoleResponse>>> = ResponseEntity.ok(DataResponseBody(roleService.findAll()))

    @Operation(summary = "역할 생성", description = "새로운 역할을 생성합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "역할 생성 성공",
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
                responseCode = "409",
                description = "이미 존재하는 역할명",
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
    @ResponseCreated(path = "/roles/{id}")
    fun createRole(
        authentication: Authentication,
        @Parameter(description = "역할 생성 정보", required = true) @RequestBody @Valid request: RoleCreateRequest,
    ): ResponseEntity<Long> = ResponseEntity.ok(roleService.save(request, authentication))

    @Operation(summary = "역할 수정", description = "기존 역할의 정보를 수정합니다")
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
                responseCode = "404",
                description = "역할을 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ), ApiResponse(
                responseCode = "409",
                description = "이미 존재하는 역할명",
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
    @PatchMapping("/{id}")
    fun updateRole(
        @PathVariable @Parameter(description = "역할 ID", required = true) id: Long,
        @Parameter(description = "역할 수정 정보", required = true) @RequestBody @Valid request: RoleUpdateRequest,
    ): ResponseEntity<Void> {
        roleService.update(id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "역할 삭제", description = "ID로 역할을 삭제합니다")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "역할 삭제 성공",
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
                responseCode = "404",
                description = "역할을 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ), ApiResponse(
                responseCode = "409",
                description = "사용 중인 역할 삭제 시도",
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
    @DeleteMapping("/{id}")
    fun deleteRole(
        @PathVariable @Parameter(description = "역할 ID", required = true) id: Long,
    ): ResponseEntity<Void> {
        roleService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
