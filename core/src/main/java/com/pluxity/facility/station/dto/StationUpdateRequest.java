package com.pluxity.facility.station.dto;

import java.util.List;

public record StationUpdateRequest(
        String name,
        String description,
        Long drawingFileId,
        Long thumbnailFileId,
        List<Long> lineIds,
        String route) {}
