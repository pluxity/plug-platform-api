package com.pluxity.file.dto;

import com.pluxity.file.entity.FileEntity;

public record FileUploadResponse(
        Long id,
        String originalFileName,
        String filePath,
        String contentType,
        String createdAt
) implements UploadResponse {
    public static FileUploadResponse from(FileEntity entity) {
        return new FileUploadResponse(
                entity.getId(),
                entity.getOriginalFileName(),
                entity.getFilePath(),
                entity.getContentType(),
                entity.getCreatedAt().toString()
        );
    }
}
