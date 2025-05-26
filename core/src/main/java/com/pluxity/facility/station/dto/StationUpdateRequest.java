package com.pluxity.facility.station.dto;

public record StationUpdateRequest(
        String name,
        String description,
        Long drawingFileId,
        Long thumbnailFileId,
        Long lineId,
        String route,
        String code) {}
