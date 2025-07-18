package com.pluxity.user.controller;

import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ErrorResponseBody;
import com.pluxity.user.dto.PermissionRequest;
import com.pluxity.user.entity.ResourceType;
import com.pluxity.user.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Controller", description = "역할 기반 리소스 인스턴스 권한 관리 API")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(
            summary = "권한 동기화",
            description = "특정 역할의 특정 리소스 타입에 대한 권한을 요청된 ID 목록으로 완전히 덮어씁니다. (가장 권장되는 방식)")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "권한 동기화 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청 (e.g., 존재하지 않는 roleId, 유효하지 않은 resourceId 포함)",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "403",
                        description = "접근 거부 (ADMIN 역할이 아님)",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public void syncPermissions(@Valid @RequestBody PermissionRequest request) {
        permissionService.syncPermissions(request);
    }

    @Operation(
            summary = "권한 부여",
            description = "특정 역할에게 하나 이상의 리소스에 대한 권한을 추가로 부여합니다. (이미 있는 권한은 무시)")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "권한 부여 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "403",
                        description = "접근 거부",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PostMapping("/grant")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public void grantPermission(@Valid @RequestBody PermissionRequest request) {
        permissionService.grantPermissionToRole(request);
    }

    @Operation(summary = "권한 회수", description = "특정 역할에게서 하나 이상의 리소스에 대한 권한을 회수합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "권한 회수 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "403",
                        description = "접근 거부",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PostMapping("/revoke")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public void revokePermission(@Valid @RequestBody PermissionRequest request) {
        permissionService.revokePermissionFromRole(request);
    }

    @Operation(
            summary = "권한 부여 가능 리소스 타입 목록 조회",
            description = "시스템에서 권한을 관리할 수 있는 모든 리소스 타입의 목록을 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "조회 성공"),
                @ApiResponse(
                        responseCode = "403",
                        description = "접근 거부",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @GetMapping("/types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DataResponseBody<List<Map<String, String>>>> getAvailableResourceTypes() {
        List<Map<String, String>> resourceTypes =
                Arrays.stream(ResourceType.values())
                        .map(
                                type ->
                                        Map.of(
                                                "key", type.name(),
                                                "value", type.getResourceName()))
                        .collect(Collectors.toList());
        // 공통 응답 DTO로 감싸서 반환
        return ResponseEntity.ok(DataResponseBody.of(resourceTypes));
    }
}
