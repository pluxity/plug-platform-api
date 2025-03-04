package com.pluxity.file.strategy.storage;

import com.pluxity.file.constant.FileType;
import com.pluxity.global.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RequiredArgsConstructor
@Slf4j
public class LocalStorageStrategy implements StorageStrategy {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public FileProcessingContext save(MultipartFile multipartFile) throws Exception {

        InputStream inputStream = multipartFile.getInputStream();

        Path tempPath = FileUtils.createTempFile(multipartFile.getOriginalFilename());
        Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);

        String contentType = FileUtils.getContentType(multipartFile);

        Path uploadTempPath = Paths.get(uploadPath, "temp");
        Path savedPath = FileUtils.saveFileToDirectory(multipartFile, uploadTempPath);

        return FileProcessingContext.builder()
                .contentType(contentType)
                .originalFilePath(tempPath)
                .originalFileName(multipartFile.getOriginalFilename())
                .savedPath(savedPath.toString())
                .build();
    }

    @Override
    public String persist(FilePersistenceContext context) throws Exception {

        return "";
    }
}