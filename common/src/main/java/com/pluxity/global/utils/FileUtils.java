package com.pluxity.global.utils;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.UUID;


@Slf4j
public class FileUtils {

    private static final String PREFIX = "pluxity-";

    public static Path saveFileToDirectory(MultipartFile multipartFile, Path targetDirectory) throws Exception {
        String uniqueFileName = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
        Path targetPath = Paths.get(targetDirectory.toString(), uniqueFileName);
        Files.createDirectories(targetPath.getParent());

        try (InputStream inputStream = new BufferedInputStream(multipartFile.getInputStream())) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return targetPath;
    }

    public static void deleteDirectoryRecursively(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(@NotNull Path directory, IOException exc) throws IOException {
                Files.delete(directory);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static Path createTempFile(String originalFileName) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String uniqueFileName = UUID.randomUUID() + "_" + originalFileName;
        Path tempFilePath = Paths.get(tempDir, uniqueFileName);

        Files.createDirectories(tempFilePath.getParent());

        return tempFilePath;
    }

    public static Path createTempDirectory(String suffix) throws IOException {
        return Files.createTempDirectory(PREFIX + suffix);
    }

    public static String getContentType(MultipartFile multipartFile) {
        return Optional.ofNullable(multipartFile.getContentType())
                .orElseGet(() -> "application/octet-stream");
    }

}