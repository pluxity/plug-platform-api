package com.pluxity.facility.station.dto;

import com.pluxity.facility.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse; // Assuming this exists and is suitable
import java.util.List;

// import lombok.Builder; // Removing builder for canonical record

public record StationResponse(
    Long id, // Added Station's own ID
    FacilityResponse facility, // Nested DTO (now simplified)
    List<FloorResponse> floors,
    List<Long> lineIds,
    List<String> featureIds,
    String route,
    String subway
) {
    // Removed explicit constructor to use default canonical constructor for record
    // This makes it cleaner for MapStruct to use, or it will use property access.
}
