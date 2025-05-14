package com.pluxity.icon.dto;

import com.pluxity.file.dto.FileResponse;
import com.pluxity.icon.entity.Icon;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record IconResponse(
        Long id,
        String name,
        FileResponse file,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static IconResponse from(Icon icon, FileResponse fileResponse) {
        return IconResponse.builder()
                .id(icon.getId())
                .name(icon.getName())
                .file(fileResponse)
                .createdAt(icon.getCreatedAt())
                .updatedAt(icon.getUpdatedAt())
                .build();
    }
}

