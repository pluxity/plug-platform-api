package com.pluxity.file.strategy.storage;

import java.nio.file.Path;
import lombok.Builder;

@Builder
public record FileProcessingContext(String contentType, Path tempPath, String originalFileName) {}
