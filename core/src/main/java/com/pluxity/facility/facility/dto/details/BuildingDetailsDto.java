package com.pluxity.facility.facility.dto.details;

import com.pluxity.facility.facility.BuildingInfo;

public record BuildingDetailsDto(int floorCount, String address) implements FacilityDetailsDto {
    public BuildingDetailsDto(BuildingInfo buildingInfo) {
        this(buildingInfo.getFloorCount(), buildingInfo.getAddress());
    }
} 