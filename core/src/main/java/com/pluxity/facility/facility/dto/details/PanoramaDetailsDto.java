package com.pluxity.facility.facility.dto.details;

import com.pluxity.facility.facility.PanoramaInfo;

public record PanoramaDetailsDto(String panoramaUrl) implements FacilityDetailsDto {
    public PanoramaDetailsDto(PanoramaInfo panoramaInfo) {
        this(panoramaInfo.getPanoramaUrl());
    }
} 