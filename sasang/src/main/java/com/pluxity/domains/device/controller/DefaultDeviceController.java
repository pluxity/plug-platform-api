package com.pluxity.domains.device.controller;

import com.pluxity.domains.device.dto.DeviceCreateRequest;
import com.pluxity.domains.device.dto.DeviceResponse;
import com.pluxity.domains.device.dto.DeviceUpdateRequest;
import com.pluxity.domains.device.service.DeviceService;
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
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "Device Controller", description = "디바이스 관리 API")
public class DefaultDeviceController {

    private final DeviceService service;

    @Operation(summary = "디바이스 생성", description = "새로운 디바이스를 생성합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "디바이스 생성 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
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
    @PostMapping
    @ResponseCreated
    public ResponseEntity<Long> create(
            @Parameter(description = "디바이스 생성 정보", required = true) @Valid @RequestBody
                    DeviceCreateRequest request) {

        Long id = service.save(request);

        return ResponseEntity.ok(id);
    }

    @Operation(summary = "디바이스 목록 조회", description = "모든 디바이스 목록을 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
                @ApiResponse(
                        responseCode = "500",
                        description = "서버 오류",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @GetMapping
    public ResponseEntity<DataResponseBody<List<DeviceResponse>>> getAll() {
        return ResponseEntity.ok(DataResponseBody.of(service.findAll()));
    }

    @Operation(summary = "디바이스 상세 조회", description = "ID로 특정 디바이스의 상세 정보를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "디바이스 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "디바이스를 찾을 수 없음",
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
    @GetMapping("/{id}")
    public ResponseEntity<DataResponseBody<DeviceResponse>> get(
            @Parameter(description = "디바이스 ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(DataResponseBody.of(service.findById(id)));
    }

    @Operation(summary = "디바이스 수정", description = "기존 디바이스의 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "디바이스 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "디바이스를 찾을 수 없음",
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
    @PatchMapping("/{id}")
    public ResponseEntity<Void> patch(
            @Parameter(description = "디바이스 ID", required = true) @PathVariable Long id,
            @Parameter(description = "디바이스 수정 정보", required = true) @Valid @RequestBody
                    DeviceUpdateRequest request) {
        service.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "디바이스 삭제", description = "ID로 디바이스를 삭제합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "디바이스 삭제 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "디바이스를 찾을 수 없음",
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "디바이스 ID", required = true) @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
