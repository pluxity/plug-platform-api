package com.pluxity.facility.facility.dto;

public record FacilityCreateRequest(
        String name, String description, Long drawingFileId, Long thumbnailFileId) {}
