package com.pluxity.file.strategy.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageStrategy {
    FileProcessingContext save(MultipartFile multipartFile) throws Exception;
    String persist(FilePersistenceContext context) throws Exception;
}
