package com.pluxity.global.utils;

import com.pluxity.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.pluxity.global.constant.ErrorCode.*;

@Slf4j
public class ZipUtils {

    public static void zip(MultipartFile file, Path bp) {
        try (FileOutputStream fos = new FileOutputStream(bp.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
            ZipEntry zipEntry = new ZipEntry(originalFilename);
            zos.putNextEntry(zipEntry);
            zos.write(file.getBytes());
            zos.closeEntry();
        } catch (Exception e) {
            log.error("압축 실패: {}", e.getMessage());
            throw new CustomException(FAILED_TO_ZIP_FILE);
        }
    }

    public static void unzip(InputStream inputStream, Path targetDirectory) throws IOException {

        Path destDirPath = targetDirectory.toAbsolutePath().normalize();

        try (ZipInputStream zis = new ZipInputStream(inputStream, StandardCharsets.UTF_8)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                Path targetFilePath = destDirPath.resolve(zipEntry.getName()).normalize();
                if (!targetFilePath.startsWith(destDirPath)) {
                    throw new IOException("ZIP 엔트리가 대상 폴더 외부에 위치합니다: " + zipEntry.getName());
                }

                if (zipEntry.isDirectory()) {
                    Files.createDirectories(targetFilePath);
                } else {
                    Files.createDirectories(targetFilePath.getParent());
                    Files.copy(zis, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }
}
