package com.pluxity.onboarding

import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.global.response.ErrorResponseBody
import com.pluxity.onboarding.dto.AdminUserCreateRequest
import com.pluxity.onboarding.dto.OnboardingFacilityRequest
import com.pluxity.onboarding.dto.OnboardingFileResponse
import com.pluxity.onboarding.service.OnboardingBuildingService
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
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
    private val buildingService: OnboardingBuildingService,
    private val collector: OnboardingCollector,
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
        @RequestBody
        @Valid request: AdminUserCreateRequest,
    ): ResponseEntity<Long> = ResponseEntity.ok(userService.createAdminUser(request))

    @Operation(
        summary = "파일 업로드",
        description = "파일을 임시 저장소에 업로드하고 File ID와 URL을 반환합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "파일 업로드 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = OnboardingFileResponse::class),
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
    @PostMapping("/files", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
        @Parameter(description = "업로드할 파일", required = true)
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<OnboardingFileResponse> = ResponseEntity.ok(fileService.uploadFile(file))

    @Operation(
        summary = "건물 생성",
        description = "새로운 건물과 층 정보를 생성합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "건물 생성 성공",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = Long::class),
                    ),
                ],
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
    @PostMapping("/buildings")
    @ResponseCreated(path = "/api/v1/onboarding/buildings/{id}")
    fun createBuilding(
        @Parameter(description = "건물 생성 요청 정보", required = true)
        @RequestBody
        @Valid request: OnboardingFacilityRequest,
    ): ResponseEntity<Long> = ResponseEntity.ok(buildingService.save(request))

    @Operation(
        summary = "임시 파일 삭제",
        description = "업로드했지만 사용하지 않는 임시 파일을 삭제합니다. TEMP 상태의 파일만 삭제 가능합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "파일 삭제 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "파일을 찾을 수 없음",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "400",
                description = "TEMP 상태가 아닌 파일은 삭제 불가",
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
    @DeleteMapping("/files/{id}")
    fun deleteTempFile(
        @Parameter(description = "삭제할 파일 ID", required = true, example = "123")
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        fileService.deleteTempFile(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Mock 데이터 수집 테스트",
        description = "Postman Mock API로 부터 데이터 수집",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "데이터 수집 성공 (일부 실패 포함 가능)",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = MockData::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "500",
                description = "모든 디바이스 수집 실패",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ErrorResponseBody::class),
                    ),
                ],
            ),
        ],
    )
    @GetMapping("/collect")
    suspend fun collectData(): ResponseEntity<List<MockData>> = ResponseEntity.ok(collector.collectData())
}
