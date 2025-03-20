package com.pluxity.building.controller;

import com.pluxity.authentication.security.CustomUserDetails;
import com.pluxity.building.dto.BuildingCreateRequest;
import com.pluxity.building.dto.BuildingListResponse;
import com.pluxity.building.service.BuildingService;
import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.global.response.CreatedResponseBody;
import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ResponseBody;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buildings")
public class BuildingController {

    private final BuildingService service;

    public BuildingController(BuildingService service) {
        this.service = service;
    }

    @GetMapping
    public DataResponseBody<List<BuildingListResponse>> getBuildings(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return DataResponseBody.of(service.findAll(userDetails.user().getId()));
    }
    
    @GetMapping("/{id}")
    public DataResponseBody<BuildingListResponse> getBuilding(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var building = service.findById(id, userDetails.user().getId());
        return DataResponseBody.of(
                BuildingListResponse.builder()
                        .id(building.getId())
                        .name(building.getName())
                        .build());
    }

    @PostMapping
    @ResponseCreated
    public ResponseEntity<ResponseBody> saveBuilding(@RequestBody BuildingCreateRequest dto) {
        Long id = service.save(dto);
        return ResponseEntity.ok(CreatedResponseBody.of(id));
    }
}
