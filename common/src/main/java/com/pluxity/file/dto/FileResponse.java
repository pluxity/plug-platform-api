package com.pluxity.file.dto;

import com.pluxity.file.constant.FileStatus;
import com.pluxity.file.constant.FileType;
import com.pluxity.file.entity.FileEntity;

import java.time.LocalDateTime;

public record FileResponse(
    Long id,
    String filePath,
    String originalFileName,
    String contentType,
    FileType fileType,
    FileStatus fileStatus,
    String fileUrl,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt
) {
    public static FileResponse from(FileEntity fileEntity, String fileUrl) {
        return new FileResponse(
            fileEntity.getId(),
            fileEntity.getFilePath(),
            fileEntity.getOriginalFileName(),
            fileEntity.getContentType(),
            fileEntity.getFileType(),
            fileEntity.getFileStatus(),
            fileUrl,
            fileEntity.getCreatedAt(),
            fileEntity.getModifiedAt()
        );
    }
    
    public static FileResponse empty() {
        return new FileResponse(
            null, null, null, null, null, null, null, null, null
        );
    }
} 