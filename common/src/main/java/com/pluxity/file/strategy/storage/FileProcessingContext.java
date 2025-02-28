package com.pluxity.file.strategy.storage;

import com.pluxity.file.constant.FileType;
import lombok.Builder;

import java.nio.file.Path;

@Builder
public record FileProcessingContext(
        FileType fileType,
        Path originalFilePath,
        String contentType,
        String savedFileName,
        String originalFileName
) {
    public FileProcessingContext(FileType fileType, Path originalFilePath, String contentType, String savedFileName, String originalFileName) {
        this.fileType = fileType;
        this.originalFilePath = originalFilePath;
        this.contentType = contentType;
        this.savedFileName = savedFileName;
        this.originalFileName = originalFileName;
    }

    public String getSavedFilePath() {
        return originalFilePath.toString();
    }
}