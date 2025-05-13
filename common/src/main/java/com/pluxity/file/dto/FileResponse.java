package com.pluxity.file.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record FileResponse(
        Long id,
        String url,
        String originalFileName,
        String contentType,
        String fileStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static FileResponse empty() {
        return new FileResponse(null, null, null, null, null, null, null);
    }
}
