package com.pluxity.domains.device_category_acl.device.controller;

import com.pluxity.domains.device_category_acl.device.dto.DeviceCategoryResponseDto;
import com.pluxity.domains.device_category_acl.device.dto.PermissionRequestDto;
import com.pluxity.domains.device_category_acl.device.service.DeviceCategoryAclService;
import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ErrorResponseBody;
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
@RequestMapping("/acl/device-categories")
@RequiredArgsConstructor
@Tag(name = "Device Category ACL Controller", description = "디바이스 카테고리 접근 제어 API")
public class DeviceCategoryAclController {

    private final DeviceCategoryAclService deviceCategoryAclService;

    @Operation(summary = "권한 관리", description = "사용자 또는 역할에 디바이스 카테고리에 대한 권한을 부여하거나 회수합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "권한 관리 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
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
    @PostMapping("/manage")
    public ResponseEntity<Void> managePermission(
            @Parameter(description = "권한 관리 요청 정보", required = true) @Valid @RequestBody
                    PermissionRequestDto request) {
        deviceCategoryAclService.managePermission(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "읽기 권한 확인", description = "현재 사용자가 특정 디바이스 카테고리에 대한 읽기 권한이 있는지 확인합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "권한 확인 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "디바이스 카테고리를 찾을 수 없음",
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
    @GetMapping("/{deviceCategoryId}/check-read")
    public ResponseEntity<DataResponseBody<Boolean>> checkReadPermission(
            @Parameter(description = "디바이스 카테고리 ID", required = true) @PathVariable
                    Long deviceCategoryId) {
        Boolean result = deviceCategoryAclService.hasReadPermission(deviceCategoryId);
        return ResponseEntity.ok(DataResponseBody.of(result));
    }

    @Operation(summary = "내 디바이스 카테고리 목록 조회", description = "현재 사용자가 접근 권한이 있는 디바이스 카테고리 목록을 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
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
    @GetMapping("/mine")
    public ResponseEntity<DataResponseBody<List<DeviceCategoryResponseDto>>> getMyDeviceCategories() {
        List<DeviceCategoryResponseDto> categories =
                deviceCategoryAclService.findAllAllowedForCurrentUser();
        return ResponseEntity.ok(DataResponseBody.of(categories));
    }
}
