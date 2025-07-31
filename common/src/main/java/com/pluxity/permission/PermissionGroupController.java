package com.pluxity.permission;

import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ErrorResponseBody;
import com.pluxity.permission.dto.PermissionGroupCreateRequest;
import com.pluxity.permission.dto.PermissionGroupResponse;
import com.pluxity.permission.dto.PermissionGroupUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/permission-groups")
@RequiredArgsConstructor
@Tag(name = "PermissionGroup Controller", description = "권한 그룹 관리 API")
public class PermissionGroupController {

    private final PermissionGroupService permissionGroupService;

    @Operation(summary = "권한 그룹 생성", description = "새로운 권한 그룹과 하위 권한들을 생성합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "권한 그룹 생성 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PostMapping
    public ResponseEntity<Void> createPermissionGroup(
            @Parameter(description = "권한 그룹 생성 정보", required = true) @Valid @RequestBody
                    PermissionGroupCreateRequest request) {
        Long groupId = permissionGroupService.create(request);
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(groupId)
                        .toUri();
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "권한 그룹 목록 조회", description = "모든 권한 그룹 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "목록 조회 성공")})
    @GetMapping
    public ResponseEntity<List<PermissionGroupResponse>> getPermissionGroups() {
        return ResponseEntity.ok(permissionGroupService.findAll());
    }

    @Operation(summary = "권한 그룹 상세 조회", description = "ID로 특정 권한 그룹의 상세 정보를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "권한 그룹 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 권한 그룹을 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @GetMapping("/{id}")
    public ResponseEntity<PermissionGroupResponse> getPermissionGroup(
            @Parameter(description = "권한 그룹 ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(permissionGroupService.findById(id));
    }

    @Operation(summary = "권한 그룹 정보 수정", description = "ID로 특정 권한 그룹의 정보를 수정합니다. (PATCH 방식)")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "권한 그룹 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 권한 그룹을 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updatePermissionGroup(
            @Parameter(description = "권한 그룹 ID", required = true) @PathVariable Long id,
            @Parameter(description = "권한 그룹 수정 정보", required = true) @Valid @RequestBody
                    PermissionGroupUpdateRequest request) {
        permissionGroupService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "권한 그룹 삭제", description = "ID로 특정 권한 그룹을 삭제합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "권한 그룹 삭제 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 권한 그룹을 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermissionGroup(
            @Parameter(description = "권한 그룹 ID", required = true) @PathVariable Long id) {
        permissionGroupService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "권한 설정 가능 리소스 타입 목록 조회",
            description = "역할에 부여할 수 있는 모든 리소스 타입의 한글 이름 목록을 조회합니다. (예: [\"시설\", \"장비 분류\"])")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "리소스 타입 목록 조회 성공")})
    @GetMapping("/resource-types")
    public ResponseEntity<DataResponseBody<List<String>>> getAvailableResourceTypes() {
        List<String> resourceTypeNames =
                Arrays.stream(ResourceType.values())
                        .map(ResourceType::getResourceName)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(DataResponseBody.of(resourceTypeNames));
    }
}
