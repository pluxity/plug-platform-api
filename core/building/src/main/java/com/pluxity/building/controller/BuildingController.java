package com.pluxity.building.controller;

import com.pluxity.building.dto.BuildingCreateRequest;
import com.pluxity.building.dto.BuildingResponse;
import com.pluxity.building.dto.BuildingUpdateRequest;
import com.pluxity.building.service.BuildingService;
import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.global.response.CreatedResponseBody;
import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ResponseBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @PostMapping
    @ResponseCreated
    public ResponseEntity<CreatedResponseBody<Long>> createBuilding(@RequestBody @Valid BuildingCreateRequest request) {
        return ResponseEntity.ok(CreatedResponseBody.of(buildingService.createBuilding(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataResponseBody<BuildingResponse>> getBuilding(@PathVariable Long id) {
        BuildingResponse response = buildingService.getBuilding(id);
        return ResponseEntity.ok(DataResponseBody.of(response));
    }

    @GetMapping
    public ResponseEntity<DataResponseBody<List<BuildingResponse>>> getAllBuildings() {
        List<BuildingResponse> responses = buildingService.getAllBuildings();
        return ResponseEntity.ok(DataResponseBody.of(responses));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseBody> updateBuilding(
            @PathVariable Long id,
            @RequestBody @Valid BuildingUpdateRequest request) {
        buildingService.updateBuilding(id, request);
        return ResponseEntity.ok(ResponseBody.of());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuilding(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return ResponseEntity.noContent().build();
    }
}
