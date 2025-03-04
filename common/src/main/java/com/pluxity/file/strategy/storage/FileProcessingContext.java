package com.pluxity.file.strategy.storage;

import lombok.Builder;

import java.nio.file.Path;

@Builder
public record FileProcessingContext(
        String contentType,
        Path originalFilePath,
        String originalFileName,
        String savedPath
) { }