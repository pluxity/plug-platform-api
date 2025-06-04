package com.pluxity.domains.station;

import com.pluxity.domains.station.dto.BusanSubwayStationResponse;
import com.pluxity.domains.station.dto.SasangStationCreateRequest;
import com.pluxity.domains.station.dto.SasangStationResponse;
import com.pluxity.domains.station.dto.SasangStationUpdateRequest;
import com.pluxity.facility.facility.dto.FacilityHistoryResponse;
import com.pluxity.facility.station.dto.StationResponseWithFeature;
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
@RequestMapping("/stations")
@RequiredArgsConstructor
@Tag(name = "Station Controller", description = "역 관리 API")
public class SasangStationController {

    private final SasangStationService service;

    @Operation(summary = "역 생성", description = "새로운 역을 생성합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "역 생성 성공"),
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
    @ResponseCreated(path = "/sasang/stations/{id}")
    public ResponseEntity<Long> create(
            @Parameter(description = "역 생성 정보", required = true) @Valid @RequestBody
                    SasangStationCreateRequest request) {

        Long id = service.save(request);

        return ResponseEntity.ok(id);
    }

    @Operation(summary = "역 목록 조회", description = "모든 역 목록을 조회합니다")
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
    public ResponseEntity<DataResponseBody<List<SasangStationResponse>>> getAll() {
        return ResponseEntity.ok(DataResponseBody.of(service.findAll()));
    }

    @Operation(summary = "역 상세 조회", description = "ID로 특정 역의 상세 정보를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "역 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "역을 찾을 수 없음",
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
    public ResponseEntity<DataResponseBody<SasangStationResponse>> get(
            @Parameter(description = "역 ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(DataResponseBody.of(service.findById(id)));
    }

    @Operation(summary = "외부 코드로 역 조회", description = "외부 코드로 특정 역의 상세 정보를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "역 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "역을 찾을 수 없음",
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
    @GetMapping("/external-code/{externalCode}")
    public ResponseEntity<DataResponseBody<SasangStationResponse>> getByExternalCode(
            @Parameter(description = "역 외부 코드", required = true) @PathVariable String externalCode) {
        return ResponseEntity.ok(DataResponseBody.of(service.findByExternalCode(externalCode)));
    }

    @Operation(summary = "특정 역 이력 조회", description = "특정 ID를 가진 역의 이력 목록을 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "이력 조회 성공",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = DataResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 역을 찾을 수 없음",
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
    @GetMapping("/{id}/history")
    public ResponseEntity<DataResponseBody<List<FacilityHistoryResponse>>> getStationHistoryById(
            @PathVariable Long id) {
        return ResponseEntity.ok(DataResponseBody.of(service.findFacilityHistories(id)));
    }

    @Operation(summary = "역 수정", description = "기존 역의 정보를 수정합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "역 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "역을 찾을 수 없음",
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
            @Parameter(description = "역 ID", required = true) @PathVariable Long id,
            @Parameter(description = "역 수정 정보", required = true) @Valid @RequestBody
                    SasangStationUpdateRequest request) {
        service.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "역 삭제", description = "ID로 역을 삭제합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "역 삭제 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "역을 찾을 수 없음",
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
            @Parameter(description = "역 ID", required = true) @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "역에 노선 추가", description = "특정 역에 노선을 추가합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "노선 추가 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "역 또는 노선을 찾을 수 없음",
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
    @PostMapping("/{stationId}/lines/{lineId}")
    public ResponseEntity<Void> addLineToStation(
            @Parameter(description = "역 ID", required = true) @PathVariable Long stationId,
            @Parameter(description = "노선 ID", required = true) @PathVariable Long lineId) {
        service.addLineToStation(stationId, lineId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "역에서 노선 제거", description = "특정 역에서 노선을 제거합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "노선 제거 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "역 또는 노선을 찾을 수 없음",
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
    @DeleteMapping("/{stationId}/lines/{lineId}")
    public ResponseEntity<Void> removeLineFromStation(
            @Parameter(description = "역 ID", required = true) @PathVariable Long stationId,
            @Parameter(description = "노선 ID", required = true) @PathVariable Long lineId) {
        service.removeLineFromStation(stationId, lineId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "역의 피처 목록 조회", description = "특정 역의 모든 피처 목록을 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "피처 목록 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "역을 찾을 수 없음",
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
    @GetMapping("/{stationId}/with-features")
    public ResponseEntity<DataResponseBody<StationResponseWithFeature>> getStationFeatures(
            @Parameter(description = "역 ID", required = true) @PathVariable Long stationId) {
        return ResponseEntity.ok(DataResponseBody.of(service.findStationWithFeatures(stationId)));
    }

    @Operation(summary = "부산 지하철 역 전체 조회", description = "부산 지하철 1-4호선의 모든 역 정보를 조회합니다")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "부산 지하철 역 목록 조회 성공"),
                @ApiResponse(
                        responseCode = "500",
                        description = "서버 오류",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @GetMapping("/busan-subway")
    public ResponseEntity<DataResponseBody<List<BusanSubwayStationResponse>>>
            getAllBusanSubwayStations() {
        return ResponseEntity.ok(DataResponseBody.of(service.findAllBusanSubwayStations()));
    }
}
