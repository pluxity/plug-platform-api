package com.pluxity.building.service;

import com.pluxity.building.repository.BuildingRepository;
import org.springframework.stereotype.Service;

@Service
public class BuildingService {

    private final BuildingRepository buildingRepository;

    public BuildingService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    public String getBuilding() {
        return "Building";
    }
}
