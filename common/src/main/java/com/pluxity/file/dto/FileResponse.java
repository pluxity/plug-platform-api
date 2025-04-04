package com.pluxity.file.dto;

import com.pluxity.file.entity.FileEntity;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FileResponse(
    Long id,
    String url,
    String originalFileName,
    String fileType,
    String contentType,
    String fileStatus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    
    public static FileResponse empty() {
        return new FileResponse(
            null, null, null, null, null, null, null, null
        );
    }
} 