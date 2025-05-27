package com.pluxity.facility.facility.dto;

public record FacilityUpdateRequest(
        String code, String name, String description, Long thumbnailFileId) {}
