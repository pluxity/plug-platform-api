package com.pluxity.permission;

import com.pluxity.global.response.ErrorResponseBody;
import com.pluxity.permission.dto.PermissionCreateRequest;
import com.pluxity.permission.dto.PermissionUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

// @RestController
// @RequestMapping("/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Controller", description = "권한 관리 API")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "권한 생성", description = "새로운 권한을 생성합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "권한 생성 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PostMapping
    public ResponseEntity<Void> createPermission(
            @Parameter(description = "권한 생성 정보", required = true) @Valid @RequestBody
                    PermissionCreateRequest request) {
        List<Long> permissionId = permissionService.create(request);
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(permissionId)
                        .toUri();
        return ResponseEntity.created(location).build();
    }

    //    @Operation(summary = "권한 목록 조회", description = "모든 권한 목록을 조회합니다.")
    //    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "목록 조회 성공")})
    //    @GetMapping
    //    public ResponseEntity<List<PermissionResponse>> getPermissions() {
    //        return ResponseEntity.ok(permissionService.findAll());
    //    }
    //
    //    @Operation(summary = "권한 상세 조회", description = "ID로 특정 권한의 상세 정보를 조회합니다.")
    //    @ApiResponses(
    //            value = {
    //                @ApiResponse(responseCode = "200", description = "권한 조회 성공"),
    //                @ApiResponse(
    //                        responseCode = "404",
    //                        description = "해당 ID의 권한을 찾을 수 없음",
    //                        content = @Content(schema = @Schema(implementation =
    // ErrorResponseBody.class)))
    //            })
    //    @GetMapping("/{id}")
    //    public ResponseEntity<PermissionResponse> getPermission(
    //            @Parameter(description = "권한 ID", required = true) @PathVariable Long id) {
    //        return ResponseEntity.ok(PermissionResponse.from(permissionService.findById(id)));
    //    }

    @Operation(summary = "권한 정보 수정", description = "ID로 특정 권한의 정보를 수정합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "권한 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 권한을 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updatePermission(
            @Parameter(description = "권한 ID", required = true) @PathVariable Long id,
            @Parameter(description = "권한 수정 정보", required = true) @Valid @RequestBody
                    PermissionUpdateRequest request) {
        permissionService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "권한 삭제", description = "ID로 특정 권한을 삭제합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "권한 삭제 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 권한을 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(
            @Parameter(description = "권한 ID", required = true) @PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
