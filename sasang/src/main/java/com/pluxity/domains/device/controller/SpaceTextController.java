package com.pluxity.domains.device.controller;

import com.pluxity.domains.device.dto.SpaceTextCreateRequest;
import com.pluxity.domains.device.dto.SpaceTextResponse;
import com.pluxity.domains.device.dto.SpaceTextUpdateRequest;
import com.pluxity.domains.device.service.SpaceTextService;
import com.pluxity.global.response.DataResponseBody;
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
@RequestMapping("/api/space-texts")
@RequiredArgsConstructor
@Tag(name = "SpaceText", description = "SpaceText 관리 API")
public class SpaceTextController {

    private final SpaceTextService spaceTextService;

    @PostMapping
    @Operation(summary = "SpaceText 생성", description = "새로운 SpaceText를 생성합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "SpaceText 생성 성공",
                        content = @Content(schema = @Schema(implementation = SpaceTextResponse.class))),
            })
    public ResponseEntity<DataResponseBody<SpaceTextResponse>> create(
            @Valid @RequestBody SpaceTextCreateRequest request) {
        SpaceTextResponse response = spaceTextService.createSpaceText(request);
        return ResponseEntity.ok(DataResponseBody.of(response));
    }

    @GetMapping
    @Operation(summary = "모든 SpaceText 조회", description = "모든 SpaceText를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<DataResponseBody<List<SpaceTextResponse>>> getAll() {
        List<SpaceTextResponse> responses = spaceTextService.getAllSpaceTexts();
        return ResponseEntity.ok(DataResponseBody.of(responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "SpaceText 조회", description = "ID로 특정 SpaceText를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "조회 성공",
                        content = @Content(schema = @Schema(implementation = SpaceTextResponse.class))),
                @ApiResponse(responseCode = "404", description = "SpaceText를 찾을 수 없음")
            })
    public ResponseEntity<DataResponseBody<SpaceTextResponse>> get(
            @Parameter(description = "SpaceText ID") @PathVariable String id) {
        SpaceTextResponse response = spaceTextService.getSpaceTextById(id);
        return ResponseEntity.ok(DataResponseBody.of(response));
    }

    @GetMapping("/facility/{facilityId}")
    @Operation(
            summary = "Facility별 SpaceText 조회",
            description = "특정 Facility에 속한 모든 SpaceText를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<DataResponseBody<List<SpaceTextResponse>>> getByFacilityId(
            @Parameter(description = "Facility ID") @PathVariable String facilityId) {
        List<SpaceTextResponse> responses = spaceTextService.getSpaceByFacilityId(facilityId);
        return ResponseEntity.ok(DataResponseBody.of(responses));
    }

    @PutMapping("/{id}")
    @Operation(summary = "SpaceText 수정", description = "SpaceText를 수정합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "수정 성공",
                        content = @Content(schema = @Schema(implementation = SpaceTextResponse.class))),
            })
    public ResponseEntity<DataResponseBody<SpaceTextResponse>> update(
            @Parameter(description = "SpaceText ID") @PathVariable String id,
            @Valid @RequestBody SpaceTextUpdateRequest request) {
        SpaceTextResponse response = spaceTextService.updateSpaceText(id, request);
        return ResponseEntity.ok(DataResponseBody.of(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "SpaceText 삭제", description = "SpaceText를 삭제합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "삭제 성공"),
                @ApiResponse(responseCode = "404", description = "SpaceText를 찾을 수 없음")
            })
    public ResponseEntity<Void> delete(
            @Parameter(description = "SpaceText ID") @PathVariable String id) {
        spaceTextService.deleteSpaceText(id);
        return ResponseEntity.noContent().build();
    }
}
