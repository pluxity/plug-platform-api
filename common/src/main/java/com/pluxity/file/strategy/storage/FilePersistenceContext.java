package com.pluxity.file.strategy.storage;

import lombok.Builder;

@Builder
public record FilePersistenceContext(
        String filePath, String newPath, String contentType, String originalFileName) {}
