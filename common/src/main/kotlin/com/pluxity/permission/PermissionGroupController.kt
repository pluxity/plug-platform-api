package com.pluxity.permission

import com.pluxity.global.response.DataResponseBody
import com.pluxity.global.response.ErrorResponseBody
import com.pluxity.permission.dto.PermissionGroupCreateRequest
import com.pluxity.permission.dto.PermissionGroupResponse
import com.pluxity.permission.dto.PermissionGroupUpdateRequest
import com.pluxity.permission.dto.ResourceTypeResponse
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/permissions")
@Tag(name = "Permission", description = "권한 관리 API")
class PermissionGroupController(
    private val permissionGroupService: PermissionGroupService,
) {
    @Operation(summary = "권한 생성", description = "새로운 권한과 하위 권한들을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "권한 생성 성공",
            ), ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PostMapping
    fun createPermissionGroup(
        @Parameter(description = "권한 생성 정보", required = true) @RequestBody request: @Valid PermissionGroupCreateRequest,
    ): ResponseEntity<Void> {
        val groupId = permissionGroupService.create(request)
        val location =
            ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(groupId)
                .toUri()
        return ResponseEntity.created(location).build()
    }

    @GetMapping
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "목록 조회 성공",
            ),
        ],
    )
    @Operation(summary = "권한 목록 조회", description = "모든 권한 목록을 조회합니다.")
    fun getPermissionGroups(): ResponseEntity<DataResponseBody<List<PermissionGroupResponse>>> =
        ResponseEntity.ok(DataResponseBody(permissionGroupService.findAll()))

    @Operation(summary = "권한 상세 조회", description = "ID로 특정 권한의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "권한 조회 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 권한을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @GetMapping("/{id}")
    fun getPermissionGroup(
        @Parameter(description = "권한 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<DataResponseBody<PermissionGroupResponse>> = ResponseEntity.ok(DataResponseBody(permissionGroupService.findById(id)))

    @Operation(summary = "권한 정보 수정", description = "ID로 특정 권한의 정보를 수정합니다. (PATCH 방식)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "권한 수정 성공",
            ), ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 권한을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @PatchMapping("/{id}")
    fun updatePermissionGroup(
        @Parameter(description = "권한 ID", required = true) @PathVariable id: Long,
        @Parameter(description = "권한 수정 정보", required = true) @RequestBody request: @Valid PermissionGroupUpdateRequest,
    ): ResponseEntity<Void> {
        permissionGroupService.update(id, request)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "권한 삭제", description = "ID로 특정 권한을 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "권한 삭제 성공",
            ), ApiResponse(
                responseCode = "404",
                description = "해당 ID의 권한을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponseBody::class))],
            ),
        ],
    )
    @DeleteMapping("/{id}")
    fun deletePermissionGroup(
        @Parameter(description = "권한 ID", required = true) @PathVariable id: Long,
    ): ResponseEntity<Void?> {
        permissionGroupService.delete(id)
        return ResponseEntity.noContent().build<Void?>()
    }

    @GetMapping("/resource-types")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "리소스 타입 목록 조회 성공",
            ), ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 요청",
                content = [
                    Content(
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
        ],
    )
    @Operation(
        summary = "권한 설정 가능 리소스 타입 목록 조회",
        description = "역할에 부여할 수 있는 모든 리소스 타입의 상세 정보(키, 이름, 엔드포인트)를 JSON 배열 형태로 조회합니다.",
    )
    fun getAvailableResourceTypes(): ResponseEntity<DataResponseBody<List<ResourceTypeResponse>>> =
        ResponseEntity.ok(
            DataResponseBody(
                ResourceType.entries.filter { it != ResourceType.NONE }.map { it.ResourceTypeResponse() },
            ),
        )
}
