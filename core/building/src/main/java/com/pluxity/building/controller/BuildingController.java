package com.pluxity.building.controller;

import com.pluxity.building.dto.BuildingCreateRequest;
import com.pluxity.building.dto.BuildingResponse;
import com.pluxity.building.dto.BuildingUpdateRequest;
import com.pluxity.building.service.BuildingService;
import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.global.response.CreatedResponseBody;
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
    public ResponseEntity<BuildingResponse> getBuilding(@PathVariable Long id) {
        BuildingResponse response = buildingService.getBuilding(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BuildingResponse>> getAllBuildings() {
        List<BuildingResponse> responses = buildingService.getAllBuildings();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BuildingResponse> updateBuilding(
            @PathVariable Long id,
            @RequestBody @Valid BuildingUpdateRequest request) {
        BuildingResponse response = buildingService.updateBuilding(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBuilding(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return ResponseEntity.noContent().build();
    }
}
