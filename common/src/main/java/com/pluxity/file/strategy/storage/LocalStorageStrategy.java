package com.pluxity.file.strategy.storage;

import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.FileUtils;
import com.pluxity.global.utils.UUIDUtils;
import com.pluxity.global.utils.ZipUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.stream.Stream;

import static com.pluxity.global.constant.ErrorCode.FAILED_TO_UPLOAD_FILE;
import static com.pluxity.global.constant.ErrorCode.FAILED_TO_ZIP_FILE;

@RequiredArgsConstructor
@Slf4j
public class LocalStorageStrategy implements StorageStrategy {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public FileProcessingContext save(MultipartFile multipartFile) throws Exception {

        Path tempPath = FileUtils.createTempFile(multipartFile.getOriginalFilename());

        try (InputStream inputStream = new BufferedInputStream(multipartFile.getInputStream())) {
            Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
        }

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

        String oldPath = context.filePath();
        String contentType = context.contentType();

        Path sourcePath = Paths.get(oldPath);
        Path targetDir = Paths.get(uploadPath, context.newPath(), UUIDUtils.generateShortUUID());

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Path targetPath = targetDir.resolve(context.originalFileName());
        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

        if (contentType.equalsIgnoreCase("application/zip") || targetPath.toString().endsWith(".zip")) {
            decompressAndMove(targetPath, targetDir);
        }

        Files.deleteIfExists(sourcePath);

        return targetDir.toString();
    }

    private void decompressAndMove(Path zipFilePath, Path baseDirPath) {
        Path tempDir = null;
        try {
            tempDir = FileUtils.createTempDirectory("unzipped");
            try (InputStream is = Files.newInputStream(zipFilePath)) {
                ZipUtils.unzip(is, tempDir);
            }

            moveDirectory(tempDir, baseDirPath);

        } catch (Exception e) {
            log.error("압축 파일 처리 중 오류 발생 (zipPath: {}): {}", zipFilePath, e.getMessage(), e);
            throw new CustomException(FAILED_TO_ZIP_FILE, "압축 파일 처리 중 오류 발생");
        } finally {
            if (tempDir != null) {
                try {
                    FileUtils.deleteDirectoryRecursively(tempDir);
                } catch (IOException ex) {
                    log.warn("임시 디렉토리 삭제 실패: {}", tempDir, ex);
                }
            }
        }
    }

    private void moveDirectory(Path sourceDir, Path targetDir) throws IOException {
        try (Stream<Path> paths = Files.walk(sourceDir)) {
            paths.filter(Files::isRegularFile).forEach(sourcePath -> {
                try {
                    Path relativePath = sourceDir.relativize(sourcePath);
                    Path targetPath = targetDir.resolve(relativePath);
                    Files.createDirectories(targetPath.getParent());
                    Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    log.error("Failed to move file {}: {}", sourcePath, e.getMessage());
                    throw new CustomException(FAILED_TO_UPLOAD_FILE, "압축 파일 이동 중 오류 발생");
                }
            });
        }
    }
}