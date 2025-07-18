package com.pluxity.facility.dto;

import com.pluxity.facility.path.FacilityPath;

public record FacilityPathResponse(Long id, String name, String type, String path) {
    public static FacilityPathResponse from(FacilityPath facilityPath) {
        return new FacilityPathResponse(
                facilityPath.getId(),
                facilityPath.getName(),
                facilityPath.getPathType().name(),
                facilityPath.getPath());
    }
}
