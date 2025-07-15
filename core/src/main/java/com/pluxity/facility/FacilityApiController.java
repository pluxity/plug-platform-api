package com.pluxity.facility;

import com.pluxity.facility.dto.FacilityAllResponse;
import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ErrorResponseBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
