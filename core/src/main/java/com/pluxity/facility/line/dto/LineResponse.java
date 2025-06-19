package com.pluxity.facility.line.dto;

// Removed imports:
// import com.fasterxml.jackson.annotation.JsonUnwrapped;
// import com.pluxity.facility.facility.Facility;
// import com.pluxity.facility.line.Line;
// import com.pluxity.global.response.BaseResponse;
// import java.util.List;
// import java.util.stream.Collectors;

public record LineResponse(
    Long id,
    String name, // Name before color as per desired structure
    String color
    // List<Long> stationIds, // Removed for simplicity for now
    // @JsonUnwrapped BaseResponse baseResponse // Removed
) {
    // Removed static from(Line line) method
}
