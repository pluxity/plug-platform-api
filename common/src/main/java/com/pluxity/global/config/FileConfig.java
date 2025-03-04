package com.pluxity.global.config;

import com.pluxity.file.repository.FileRepository;
import com.pluxity.file.service.FileService;
import com.pluxity.file.service.SbmFileService;
import com.pluxity.file.strategy.storage.LocalStorageStrategy;
import com.pluxity.file.strategy.storage.S3StorageStrategy;
import com.pluxity.file.strategy.storage.StorageStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class FileConfig {

    @Bean
    public FileService fileService(StorageStrategy storageStrategy, FileRepository fileRepository, SbmFileService sbmFileService, S3Config s3Config, S3Presigner s3Presigner) {
        return new FileService(s3Presigner, s3Config, storageStrategy, fileRepository, sbmFileService);
    }
    @Bean
    @ConditionalOnProperty(name = "file.storage-strategy", havingValue = "local")
    public StorageStrategy localStorageStrategy() {
        return new LocalStorageStrategy();
    }
    @Bean
    @ConditionalOnProperty(name = "file.storage-strategy", havingValue = "s3")
    public StorageStrategy s3StorageStrategy(S3Config s3Config, S3Client s3Client) {
        return new S3StorageStrategy(s3Config, s3Client);
    }

}
