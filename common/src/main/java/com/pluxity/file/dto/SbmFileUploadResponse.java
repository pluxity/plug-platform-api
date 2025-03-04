package com.pluxity.file.dto;

import com.pluxity.file.entity.FileEntity;

import java.util.List;

public record SbmFileUploadResponse(
        Long id,
        String filePath,
        String originalFileName,
        String contentType,
        String createdAt,
        List<SbmFloorGroup> floorList
) implements UploadResponse {

    public static SbmFileUploadResponse from(FileEntity entity, List<SbmFloorGroup> floorList) {
        return new SbmFileUploadResponse(
                entity.getId(),
                entity.getFilePath(),
                entity.getOriginalFileName(),
                entity.getContentType(),
                entity.getCreatedAt().toString(),
                floorList
        );
    }

}
