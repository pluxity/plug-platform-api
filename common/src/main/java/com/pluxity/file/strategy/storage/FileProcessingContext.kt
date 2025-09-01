package com.pluxity.file.strategy.storage

import java.nio.file.Path

data class FileProcessingContext(
    val contentType: String,
    val tempPath: Path,
    val originalFileName: String,
)
