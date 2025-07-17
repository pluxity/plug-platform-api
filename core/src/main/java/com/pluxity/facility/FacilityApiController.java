package com.pluxity.facility;

import com.pluxity.facility.dto.FacilityAllResponse;
import com.pluxity.facility.dto.FacilityDrawingUpdateRequest;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/facilities")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Facility Controller", description = "시설 관리 API")
public class FacilityApiController {

    private final FacilityApiService facilityService;

    @Operation(summary = "시설 목록 조회", description = "모든 시설 목록을 조회합니다")
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
    public ResponseEntity<DataResponseBody<FacilityAllResponse>> getFacilities() {
        return ResponseEntity.ok(DataResponseBody.of(facilityService.findAll()));
    }

    @Operation(summary = "시설 도면 정보 수정", description = "시설 도면 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "도면 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "시설을 찾을 수 없음",
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
    @PatchMapping("/drawing/{id}")
    public ResponseEntity<Void> patch(
            @Parameter(description = "시설물 ID", required = true) @PathVariable Long id,
            @Parameter(description = "도면 수정 정보", required = true) @Valid @RequestBody
                    FacilityDrawingUpdateRequest request) {
        facilityService.updateDrawingFile(id, request);
        return ResponseEntity.noContent().build();
    }
}
