package com.pluxity.facility.controller;

import com.pluxity.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.dto.FacilityUpdateRequest;
import com.pluxity.facility.service.FacilityService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/facilities")
@RequiredArgsConstructor
@Tag(name = "Facility Controller", description = "시설 관리 API")
public class FacilityController {

    private final FacilityService facilityService;

    @Operation(summary = "시설 생성", description = "새로운 시설을 생성합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "시설 생성 성공",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class)))
    })
    @PostMapping
    @ResponseCreated
    public ResponseEntity<Long> createFacility(
            @Parameter(description = "시설 생성 정보", required = true)
            @RequestBody @Valid FacilityCreateRequest request) {
        return ResponseEntity.ok(facilityService.createFacility(request));
    }

    @Operation(summary = "시설 조회", description = "ID로 시설을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "시설 조회 성공"),
            @ApiResponse(responseCode = "400", description = "파라미터 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class))),
            @ApiResponse(responseCode = "404", description = "시설을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<DataResponseBody<FacilityResponse>> getFacility(
            @Parameter(description = "시설 ID", required = true)
            @PathVariable Long id) {
        FacilityResponse response = facilityService.getFacility(id);
        return ResponseEntity.ok(DataResponseBody.of(response));
    }

    @Operation(summary = "모든 시설 조회", description = "모든 시설 목록을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "시설 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "파라미터 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class))),
            @ApiResponse(responseCode = "404", description = "시설을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class)))
    })
    @GetMapping
    public ResponseEntity<DataResponseBody<List<FacilityResponse>>> getAllFacilitys() {
        List<FacilityResponse> responses = facilityService.getAllFacilities();
        return ResponseEntity.ok(DataResponseBody.of(responses));
    }

    @Operation(summary = "시설 정보 수정", description = "시설 정보를 수정합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "시설 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class))),
            @ApiResponse(responseCode = "404", description = "시설을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateFacility(
            @Parameter(description = "시설 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "시설 수정 정보", required = true)
            @RequestBody @Valid FacilityUpdateRequest request) {
        facilityService.updateFacility(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "시설 삭제", description = "시설을 삭제합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "시설 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "시설을 찾을 수 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseBody.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacility(
            @Parameter(description = "시설 ID", required = true)
            @PathVariable Long id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.noContent().build();
    }
}