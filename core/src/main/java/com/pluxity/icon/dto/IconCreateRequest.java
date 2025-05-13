package com.pluxity.icon.dto;

public record IconCreateRequest(
        String name,
        String description,
        Long fileId
) {
}
