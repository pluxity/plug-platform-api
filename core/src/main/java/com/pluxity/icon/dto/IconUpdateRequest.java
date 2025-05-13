package com.pluxity.icon.dto;

public record IconUpdateRequest(
        String name,
        String description,
        Long fileId
) {
} 