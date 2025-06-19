package com.pluxity.domains.sasang.station.dto;

import com.pluxity.facility.station.dto.StationResponse; // Core DTO for station details

public record SasangStationResponse(
    Long id, // SasangStation's own ID
    StationResponse station, // Nested StationResponse from core module
    String externalCode
) {
    // Removed static of() method, MapStruct will handle mapping.
}
