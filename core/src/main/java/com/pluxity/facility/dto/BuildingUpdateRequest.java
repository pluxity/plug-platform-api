package com.pluxity.facility.dto;

public record BuildingUpdateRequest(
        String name, String description, Long drawingFileId, Long thumbnailFileId) {}
