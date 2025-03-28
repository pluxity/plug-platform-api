package com.pluxity.building.dto;

import com.pluxity.building.entity.Building;
import com.pluxity.file.dto.FileResponse;

import java.time.LocalDateTime;

public record BuildingResponse(
    Long id,
    String name,
    String description,
    FileResponse file,
    FileResponse thumbnail,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static BuildingResponse from(Building building, FileResponse file, FileResponse thumbnail) {
        return new BuildingResponse(
            building.getId(),
            building.getName(),
            building.getDescription(),
            file != null ? file : FileResponse.empty(),
            thumbnail != null ? thumbnail : FileResponse.empty(),
            building.getCreatedAt(),
            building.getUpdatedAt()
        );
    }
} 