package com.pluxity.building.dto;

import jakarta.validation.constraints.NotBlank;

public record BuildingCreateRequest(
    @NotBlank(message = "건물 이름은 필수입니다.")
    String name,
    
    String description,
    
    Long fileId,
    
    Long thumbnailId
) {
    public static BuildingCreateRequest of(String name, String description, Long fileId, Long thumbnailId) {
        return new BuildingCreateRequest(name, description, fileId, thumbnailId);
    }
}
