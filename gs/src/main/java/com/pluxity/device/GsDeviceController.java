package com.pluxity.device;

import com.pluxity.device.dto.GsDeviceCreateRequest;
import com.pluxity.device.dto.GsDeviceResponse;
import com.pluxity.device.dto.GsDeviceUpdateRequest;
import com.pluxity.global.annotation.ResponseCreated;
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
@RequestMapping("/devices")
@RequiredArgsConstructor
@Tag(name = "Device Controller", description = "디바이스 관리 API")
public class GsDeviceController {

    private final GsDeviceService gsDeviceService;

    @Operation(summary = "디바이스 생성", description = "새로운 디바이스를 생성합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "디바이스 생성 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PostMapping
    @ResponseCreated(path = "/devices/{id}")
    public ResponseEntity<String> create(
            @Parameter(description = "디바이스 생성 정보", required = true) @Valid @RequestBody
                    GsDeviceCreateRequest request) {
        String id = gsDeviceService.save(request);
        return ResponseEntity.ok(id);
    }

    @Operation(summary = "디바이스 목록 조회", description = "모든 디바이스 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "목록 조회 성공")})
    @GetMapping
    public ResponseEntity<DataResponseBody<List<GsDeviceResponse>>> getAll() {
        return ResponseEntity.ok(DataResponseBody.of(gsDeviceService.findAll()));
    }

    @Operation(summary = "디바이스 상세 조회", description = "ID로 특정 디바이스의 상세 정보를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "디바이스 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 디바이스를 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @GetMapping("/{id}")
    public ResponseEntity<DataResponseBody<GsDeviceResponse>> getById(
            @Parameter(description = "디바이스 ID", required = true) @PathVariable String id) {
        return ResponseEntity.ok(DataResponseBody.of(gsDeviceService.findById(id)));
    }

    @Operation(summary = "디바이스 정보 수정", description = "ID로 특정 디바이스의 정보를 수정합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "디바이스 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 디바이스를 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @Parameter(description = "디바이스 ID", required = true) @PathVariable String id,
            @Parameter(description = "디바이스 수정 정보", required = true) @Valid @RequestBody
                    GsDeviceUpdateRequest request) {
        gsDeviceService.putUpdate(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "디바이스 삭제", description = "ID로 특정 디바이스를 삭제합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "디바이스 삭제 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 디바이스를 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "`디바이스 ID", required = true) @PathVariable String id) {
        gsDeviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "디바이스에 카테고리 할당", description = "특정 디바이스에 카테고리를 할당(연결)합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "카테고리 할당 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 디바이스 또는 카테고리를 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PatchMapping("/{deviceId}/category/{categoryId}")
    public ResponseEntity<Void> assignCategory(
            @Parameter(description = "디바이스 ID", required = true) @PathVariable String deviceId,
            @Parameter(description = "할당할 카테고리 ID", required = true) @PathVariable Long categoryId) {
        gsDeviceService.assignCategory(deviceId, categoryId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "디바이스의 카테고리 제거", description = "특정 디바이스에 할당된 카테고리를 제거(연결 해제)합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "카테고리 제거 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 디바이스를 찾을 수 없거나, 디바이스에 할당된 카테고리가 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @DeleteMapping("/{deviceId}/category")
    public ResponseEntity<Void> removeCategory(
            @Parameter(description = "디바이스 ID", required = true) @PathVariable String deviceId) {
        gsDeviceService.removeCategory(deviceId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "디바이스 상세 조회", description = "Feature ID로 특정 디바이스의 상세 정보를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "디바이스 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 디바이스를 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @GetMapping("/features/{featureId}")
    public ResponseEntity<DataResponseBody<GsDeviceResponse>> getByFeatureId(
            @Parameter(description = "Feature ID", required = true) @PathVariable String featureId) {
        return ResponseEntity.ok(DataResponseBody.of(gsDeviceService.getByFeatureId(featureId)));
    }
}
