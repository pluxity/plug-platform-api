package com.pluxity.global.config

import com.pluxity.file.repository.FileRepository
import com.pluxity.file.service.FileService
import com.pluxity.file.strategy.storage.LocalStorageStrategy
import com.pluxity.file.strategy.storage.S3StorageStrategy
import com.pluxity.file.strategy.storage.StorageStrategy
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@Configuration
class FileConfig {
    @Bean
    fun fileService(
        storageStrategy: StorageStrategy,
        fileRepository: FileRepository,
        s3Config: S3Config,
        s3Presigner: S3Presigner,
    ): FileService = FileService(s3Presigner, s3Config, storageStrategy, fileRepository)

    @Bean
    @ConditionalOnProperty(name = ["file.storage-strategy"], havingValue = "local")
    fun localStorageStrategy(): StorageStrategy = LocalStorageStrategy()

    @Bean
    @ConditionalOnProperty(name = ["file.storage-strategy"], havingValue = "s3")
    fun s3StorageStrategy(
        s3Config: S3Config,
        s3Client: S3Client,
    ): StorageStrategy = S3StorageStrategy(s3Config, s3Client)
}
