package com.pluxity.file.strategy.storage;

import com.pluxity.file.constant.FileType;
import org.springframework.web.multipart.MultipartFile;

public interface StorageStrategy {
    FileProcessingContext save(MultipartFile multipartFile, FileType fileType);
}
