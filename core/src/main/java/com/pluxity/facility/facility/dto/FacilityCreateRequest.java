package com.pluxity.facility.facility.dto;

public record FacilityCreateRequest(
        String name, String code, String description, Long drawingFileId, Long thumbnailFileId) {}
