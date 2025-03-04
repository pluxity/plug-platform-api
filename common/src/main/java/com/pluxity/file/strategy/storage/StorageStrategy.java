package com.pluxity.file.strategy.storage;

public interface StorageStrategy {
    String save(FileProcessingContext context) throws Exception;
    String persist(FilePersistenceContext context) throws Exception;
}
