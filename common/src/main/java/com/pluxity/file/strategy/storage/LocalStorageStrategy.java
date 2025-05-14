package com.pluxity.file.strategy.storage;

import static com.pluxity.global.constant.ErrorCode.FAILED_TO_UPLOAD_FILE;
import static com.pluxity.global.constant.ErrorCode.FAILED_TO_ZIP_FILE;

import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.FileUtils;
import com.pluxity.global.utils.UUIDUtils;
import com.pluxity.global.utils.ZipUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
@Slf4j
public class LocalStorageStrategy implements StorageStrategy {

    @Value("${file.local.path}")
    private String uploadPath;

    @Override
    public String save(FileProcessingContext context) {
        try {
            String uniqueFileName = UUIDUtils.generateUUID();
            String fileExtension = FileUtils.getFileExtension(context.originalFileName());
            String fileName = uniqueFileName + fileExtension;

            // 임시저장을 위한 temp 디렉토리 경로 (uploadPath/temp)
            Path tempDir = Paths.get(uploadPath, "temp");
            Files.createDirectories(tempDir);
            Path filePath = tempDir.resolve(fileName);

            Files.copy(context.tempPath(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "temp/" + fileName;
        } catch (Exception e) {
            log.error("Failed to save file: {}", e.getMessage());
            throw new CustomException(FAILED_TO_UPLOAD_FILE);
        }
    }

    @Override
    public String persist(FilePersistenceContext context) {
        try {
            Path sourcePath = Paths.get(uploadPath, context.filePath());
            Path targetDir = Paths.get(uploadPath, context.newPath());

            String fileName = UUIDUtils.generateShortUUID();
            String extension = FileUtils.getFileExtension(context.originalFileName());

            Path targetPath = targetDir.resolve(fileName + extension);

            Files.createDirectories(targetDir);
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            return context.newPath() + targetPath.getFileName();
        } catch (Exception e) {
            log.error("Failed to persist file: {}", e.getMessage());
            throw new CustomException(FAILED_TO_UPLOAD_FILE);
        }
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
            paths
                    .filter(Files::isRegularFile)
                    .forEach(
                            sourcePath -> {
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
