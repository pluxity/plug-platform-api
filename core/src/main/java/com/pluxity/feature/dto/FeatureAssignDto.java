package com.pluxity.feature.dto;

public record FeatureAssignDto(String id) {
    public FeatureAssignDto {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("id는 필수입니다");
        }
    }
}
