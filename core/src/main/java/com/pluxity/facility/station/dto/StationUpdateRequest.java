package com.pluxity.facility.station.dto;

import java.util.List;

public record StationUpdateRequest(
        String name,
        String description,
        Long drawingFileId,
        Long thumbnailFileId,
        List<Long> lineIds,
        String route) {

    public static StationUpdateRequest of(
            String name, String description, Long thumbnailFileId, List<Long> lineIds, String route) {
        return new StationUpdateRequest(name, description, null, thumbnailFileId, lineIds, route);
    }
}
