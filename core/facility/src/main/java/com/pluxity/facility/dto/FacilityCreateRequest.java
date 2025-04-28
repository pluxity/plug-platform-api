package com.pluxity.facility.dto;

import jakarta.validation.constraints.NotBlank;

public record FacilityCreateRequest(
    @NotBlank(message = "시설 이름은 필수입니다.")
    String name,
    
    String description,
    
    Long fileId,
    
    Long thumbnailId
) {
    public static FacilityCreateRequest of(String name, String description, Long fileId, Long thumbnailId) {
        return new FacilityCreateRequest(name, description, fileId, thumbnailId);
    }
}
