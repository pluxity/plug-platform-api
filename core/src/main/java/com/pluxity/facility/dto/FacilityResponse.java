package com.pluxity.facility.dto;

import com.pluxity.facility.entity.Facility;
import com.pluxity.file.dto.FileResponse;

import java.time.LocalDateTime;

public record FacilityResponse(
    Long id,
    String name,
    String description,
    FileResponse drawing,
    FileResponse thumbnail,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static FacilityResponse from(Facility facility, FileResponse drawing, FileResponse thumbnail) {
        return new FacilityResponse(
            facility.getId(),
            facility.getName(),
            facility.getDescription(),
            drawing != null ? drawing : FileResponse.empty(),
            thumbnail != null ? thumbnail : FileResponse.empty(),
            facility.getCreatedAt(),
            facility.getUpdatedAt()
        );
    }
} 