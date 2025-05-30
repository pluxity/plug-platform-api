package com.pluxity.domains.station.dto;

import java.util.List;

public record SasangStationUpdateRequest(
        String name,
        String description,
        Long drawingFileId,
        Long thumbnailFileId,
        List<Long> lineIds,
        String route,
        String externalCode) {}
