package com.pluxity.file.strategy.storage;

import com.pluxity.file.constant.FileType;
import com.pluxity.global.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
@Slf4j
public class LocalStorageStrategy implements StorageStrategy {
    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public FileProcessingContext save(MultipartFile multipartFile, FileType fileType) throws Exception {
        String subDirectory = getSubDirectory(fileType);
        Path targetDirectory = Paths.get(uploadPath, subDirectory);
        Files.createDirectories(targetDirectory);

        String contentType = FileUtils.getContentType(multipartFile);

        // 파일 저장
        Path savedFile = FileUtils.saveFileToDirectory(multipartFile, targetDirectory);
        return new FileProcessingContext(fileType, savedFile, contentType, savedFile.getFileName().toString(), multipartFile.getOriginalFilename());
    }
    private String getSubDirectory(FileType fileType) {
        switch (fileType) {
            case THUMBNAIL:
                return "thumbnails";
            case DRAWING:
                return "drawings";
            case ICON:
                return "icons";
            case SBM:
                return "sbm";
            default:
                throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }
    }
}