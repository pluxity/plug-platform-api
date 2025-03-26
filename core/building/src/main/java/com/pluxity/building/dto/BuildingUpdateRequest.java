package com.pluxity.building.dto;

import jakarta.validation.constraints.NotBlank;

public record BuildingUpdateRequest(
    @NotBlank(message = "건물 이름은 필수입니다.")
    String name,
    
    String description,
    
    Long fileId,
    
    Long thumbnailId
) {
    public static BuildingUpdateRequest of(String name, String description, Long fileId, Long thumbnailId) {
        return new BuildingUpdateRequest(name, description, fileId, thumbnailId);
    }
} 