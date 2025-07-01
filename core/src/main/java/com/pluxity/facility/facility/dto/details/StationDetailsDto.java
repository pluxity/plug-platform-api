package com.pluxity.facility.facility.dto.details;

import com.pluxity.facility.facility.StationInfo;

public record StationDetailsDto(String lineName) implements FacilityDetailsDto {
    public StationDetailsDto(StationInfo stationInfo) {
        this(stationInfo.getLineName());
    }
} 