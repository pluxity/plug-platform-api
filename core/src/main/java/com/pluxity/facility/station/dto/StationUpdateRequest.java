package com.pluxity.facility.station.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record StationUpdateRequest(
        String name,
        String description,
        Long drawingFileId,
        Long thumbnailFileId,
        List<Long> lineIds,
        String route
) {

}
