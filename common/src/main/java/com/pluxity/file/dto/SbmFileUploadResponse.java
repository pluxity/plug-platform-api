package com.pluxity.file.dto;

import com.pluxity.file.entity.FileEntity;

import java.util.List;

public record SbmFileUploadResponse(
        Long id,
        String originalFileName,
        String filePath,
        String type,
        String createdAt,
        List<SbmFloorGroup> floorList
) implements UploadResponse {

    public static SbmFileUploadResponse from(FileEntity entity, List<SbmFloorGroup> floorList) {
        return new SbmFileUploadResponse(
                entity.getId(),
                entity.getOriginalFileName(),
                entity.getFilePath(),
                entity.getFileType(),
                entity.getCreatedAt().toString(),
                floorList
        );
    }

}
