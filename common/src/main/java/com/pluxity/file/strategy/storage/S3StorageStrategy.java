package com.pluxity.file.strategy.storage;

import com.pluxity.file.constant.FileType;
import com.pluxity.global.config.S3Config;
import com.pluxity.global.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class S3StorageStrategy implements StorageStrategy {

    private final S3Config s3Config;
    private final S3Client s3Client;

    @Override
    public FileProcessingContext save(MultipartFile multipartFile) throws Exception {

        InputStream inputStream = multipartFile.getInputStream();

        Path tempPath = FileUtils.createTempFile(multipartFile.getOriginalFilename());
        Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);

        String originalFileName = multipartFile.getOriginalFilename();
        String contentType = FileUtils.getContentType(multipartFile);

        String s3Key = "temp/" + UUID.randomUUID() + "/" + originalFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(s3Key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(tempPath.toFile()));

        return FileProcessingContext.builder()
                .contentType(contentType)
                .originalFilePath(tempPath)
                .originalFileName(multipartFile.getOriginalFilename())
                .savedPath(s3Key)
                .build();
    }
}
