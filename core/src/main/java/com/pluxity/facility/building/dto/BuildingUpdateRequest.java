package com.pluxity.facility.building.dto;

public record BuildingUpdateRequest(
        String name, String description, Long drawingFileId, Long thumbnailFileId) {}
