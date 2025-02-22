package com.pluxity.building.controller;

import com.pluxity.building.dto.BuildingResponseDto;
import com.pluxity.building.service.BuildingService;
import com.pluxity.global.response.DataResponseBody;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/buildings")
@ConditionalOnProperty(name = "core.building.enabled", havingValue = "true", matchIfMissing = true)
public class BuildingController {

    private final BuildingService service;

    public BuildingController(BuildingService service) {
        this.service = service;
    }

    @GetMapping
    public DataResponseBody<BuildingResponseDto> getBuilding() {
        return DataResponseBody.of(
                BuildingResponseDto.builder().code("code").name("name").address("address").build());
    }
}
