package com.pluxity.feature.dto;

public record FeatureAssignDto(String id, String code) {
    public FeatureAssignDto {
        if (id == null && code == null) {
            throw new IllegalArgumentException("id 또는 code 중 하나는 반드시 제공되어야 합니다");
        }
    }
}
