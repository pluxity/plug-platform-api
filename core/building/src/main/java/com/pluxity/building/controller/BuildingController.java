package com.pluxity.building.controller;

import com.pluxity.building.dto.BuildingCreateRequest;
import com.pluxity.building.dto.BuildingListResponse;
import com.pluxity.building.service.BuildingService;
import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.global.response.CreatedResponseBody;
import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ResponseBody;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.pluxity.global.constant.SuccessCode.SUCCESS_CREATED;

@RestController
@RequestMapping("/buildings")
@ConditionalOnProperty(name = "core.building.enabled", havingValue = "true", matchIfMissing = true)
public class BuildingController {

    private final BuildingService service;

    public BuildingController(BuildingService service) {
        this.service = service;
    }

    @GetMapping
    public DataResponseBody<BuildingListResponse> getBuilding() {
        return DataResponseBody.of(
                BuildingListResponse.builder().id(1L).name("building").build());
    }

    @PostMapping
    @ResponseCreated
    public ResponseEntity<ResponseBody> saveBuilding(@RequestBody BuildingCreateRequest dto) {
        Long id = service.save(dto);
        return ResponseEntity.ok(CreatedResponseBody.of(id));
    }
}
