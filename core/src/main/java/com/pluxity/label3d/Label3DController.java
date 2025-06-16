package com.pluxity.label3d;

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
@RequestMapping("/label-3d")
@RequiredArgsConstructor
@Tag(name = "Label3D", description = "Label3D 관리 API")
public class Label3DController {

    private final Label3DService label3DService;

    @PostMapping
    @Operation(summary = "Label3D 생성", description = "새로운 Label3D를 생성합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Label3D 생성 성공",
                        content = @Content(schema = @Schema(implementation = Label3DResponse.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
            })
    public ResponseEntity<DataResponseBody<Label3DResponse>> create(
            @Valid @RequestBody Label3DCreateRequest request) {
        Label3DResponse response = label3DService.createLabel3D(request);
        return ResponseEntity.ok(DataResponseBody.of(response));
    }

    @GetMapping
    @Operation(summary = "모든 Label3D 조회", description = "모든 Label3D를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<DataResponseBody<List<Label3DResponse>>> getAll() {
        List<Label3DResponse> responses = label3DService.getAllLabel3Ds();
        return ResponseEntity.ok(DataResponseBody.of(responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Label3D 조회", description = "ID로 특정 Label3D를 조회합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "조회 성공",
                        content = @Content(schema = @Schema(implementation = Label3DResponse.class))),
                @ApiResponse(responseCode = "404", description = "Label3D를 찾을 수 없음")
            })
    public ResponseEntity<DataResponseBody<Label3DResponse>> get(
            @Parameter(description = "Label3D ID") @PathVariable String id) {
        Label3DResponse response = label3DService.getLabel3DById(id);
        return ResponseEntity.ok(DataResponseBody.of(response));
    }

    @GetMapping("/facility/{facilityId}")
    @Operation(summary = "Facility별 Label3D 조회", description = "특정 Facility에 속한 모든 Label3D를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<DataResponseBody<List<Label3DResponse>>> getByFacilityId(
            @Parameter(description = "Facility ID") @PathVariable String facilityId) {
        List<Label3DResponse> responses = label3DService.getLabel3DsByFacilityId(facilityId);
        return ResponseEntity.ok(DataResponseBody.of(responses));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Label3D 수정", description = "Label3D를 수정합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "수정 성공",
                        content = @Content(schema = @Schema(implementation = Label3DResponse.class))),
                @ApiResponse(responseCode = "404", description = "Label3D를 찾을 수 없음")
            })
    public ResponseEntity<DataResponseBody<Label3DResponse>> update(
            @Parameter(description = "Label3D ID") @PathVariable String id,
            @Valid @RequestBody Label3DUpdateRequest request) {
        Label3DResponse response = label3DService.updateLabel3D(id, request);
        return ResponseEntity.ok(DataResponseBody.of(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Label3D 삭제", description = "Label3D를 삭제합니다.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "삭제 성공"),
                @ApiResponse(responseCode = "404", description = "Label3D를 찾을 수 없음")
            })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Label3D ID") @PathVariable String id) {
        label3DService.deleteLabel3D(id);
        return ResponseEntity.noContent().build();
    }
}
