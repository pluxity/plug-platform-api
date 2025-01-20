package com.pluxity.building.service;

import com.pluxity.building.dto.BuildingResponseDto;
import com.pluxity.building.repository.BuildingRepository;
import org.springframework.stereotype.Service;

@Service
public class BuildingService {

    private final BuildingRepository buildingRepository;

    public BuildingService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    public BuildingResponseDto getBuilding() {
        // TODO: Implement actual building retrieval logic
        return new BuildingResponseDto("Sample Building", "B001", "Sample Address");
    }
}
