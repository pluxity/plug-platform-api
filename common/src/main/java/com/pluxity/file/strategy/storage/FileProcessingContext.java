package com.pluxity.file.strategy.storage;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@Builder
public record FileProcessingContext(
        String contentType,
        Path tempPath,
        String originalFileName
) { }