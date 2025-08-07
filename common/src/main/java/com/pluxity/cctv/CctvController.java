package com.pluxity.cctv;

import com.pluxity.cctv.dto.CctvCreateRequest;
import com.pluxity.cctv.dto.CctvResponse;
import com.pluxity.cctv.dto.CctvUpdateRequest;
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
@RequestMapping("/cctvs")
@RequiredArgsConstructor
@Tag(name = "CCTV Controller", description = "CCTV 관리 API")
public class CctvController {

    private final CctvService cctvService;

    @Operation(summary = "CCTV 생성", description = "새로운 CCTV를 생성합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "CCTV 생성 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PostMapping
    @ResponseCreated(path = "/cctvs/{id}")
    public ResponseEntity<String> create(
            @Parameter(description = "CCTV 생성 정보", required = true) @Valid @RequestBody
                    CctvCreateRequest request) {
        String id = cctvService.create(request);
        return ResponseEntity.ok(id);
    }

    @Operation(summary = "CCTV 목록 조회", description = "모든 CCTV 목록을 조회합니다.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "목록 조회 성공")})
    @GetMapping
    public ResponseEntity<DataResponseBody<List<CctvResponse>>> getAll() {
        return ResponseEntity.ok(DataResponseBody.of(cctvService.findAll()));
    }

    @Operation(summary = "CCTV 상세 조회", description = "ID로 특정 CCTV의 상세 정보를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "CCTV 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 디바이스를 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @GetMapping("/{id}")
    public ResponseEntity<DataResponseBody<CctvResponse>> getById(
            @Parameter(description = "CCTV ID", required = true) @PathVariable String id) {
        return ResponseEntity.ok(DataResponseBody.of(cctvService.getById(id)));
    }

    @Operation(summary = "CCTV 상세 조회", description = "Feature ID로 특정 CCTV의 상세 정보를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "CCTV 조회 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 디바이스를 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @GetMapping("/features/{featureId}")
    public ResponseEntity<DataResponseBody<CctvResponse>> getByFeatureId(
            @Parameter(description = "Feature ID", required = true) @PathVariable String featureId) {
        return ResponseEntity.ok(DataResponseBody.of(cctvService.getByFeatureId(featureId)));
    }

    @Operation(summary = "CCTV 정보 수정", description = "ID로 특정 CCTV의 정보를 수정합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "CCTV 수정 성공"),
                @ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 CCTV를 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @Parameter(description = "CCTV ID", required = true) @PathVariable String id,
            @Parameter(description = "CCTV 수정 정보", required = true) @Valid @RequestBody
                    CctvUpdateRequest request) {
        cctvService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "CCTV 삭제", description = "ID로 특정 CCTV를 삭제합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "CCTV 삭제 성공"),
                @ApiResponse(
                        responseCode = "404",
                        description = "해당 ID의 CCTV를 찾을 수 없음",
                        content = @Content(schema = @Schema(implementation = ErrorResponseBody.class)))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "CCTV ID", required = true) @PathVariable String id) {
        cctvService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
