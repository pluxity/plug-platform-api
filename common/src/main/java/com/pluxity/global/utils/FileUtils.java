package com.pluxity.global.utils;

import com.pluxity.global.exception.CustomException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.Optional;

import static com.pluxity.global.constant.ErrorCode.INVALID_FILE_TYPE;


@Slf4j
public class FileUtils {

    private static final String PREFIX = "pluxity-";

    public static void checkExcel(Path path) throws IOException {
        String ext = getExtension(path);
        if (!(ext.equals("xls") || ext.equals("xlsx"))) {
            Files.delete(path);
            log.error("{}: {}",INVALID_FILE_TYPE.getMessage(), ext);
            throw new CustomException(INVALID_FILE_TYPE, "적절하지 않은 파일 유형: " + ext);
        }
    }

    public static void checkImage(Path path) {
        try {

            InputStream inputStream = Files.newInputStream(path);
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
            Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);

            if (!imageReaders.hasNext()) {
                Files.delete(path);
                log.error("{}: {}",INVALID_FILE_TYPE.getMessage(), "IMAGE");
                throw new CustomException(INVALID_FILE_TYPE, "적절하지 않은 파일 유형: 이미지");
            }

        } catch (IOException e) {
            log.error("{}: {}",INVALID_FILE_TYPE.getMessage(), "IMAGE");
            throw new CustomException(INVALID_FILE_TYPE, "적절하지 않은 파일 유형: 이미지");
        }
    }

    public static void checkImage(InputStream inputStream) {
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
            Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);

            if (!imageReaders.hasNext()) {
                log.error("{}: {}",INVALID_FILE_TYPE.getMessage(), "IMAGE");
                throw new CustomException(INVALID_FILE_TYPE, "적절하지 않은 파일 유형: 이미지");
            }

        } catch (IOException e) {
            log.error("{}: {}",INVALID_FILE_TYPE.getMessage(), "IMAGE");
            throw new CustomException(INVALID_FILE_TYPE, "적절하지 않은 파일 유형: 이미지");
        }
    }

    public static void checkExtractable(MultipartFile file) {
        int fileSignature;
        try (InputStream inputStream = file.getInputStream()) {
            fileSignature = new DataInputStream(inputStream).readInt();
            if (fileSignature != 0x504B0304 && fileSignature != 0x504B0506 && fileSignature != 0x504B0708) {
                throw new CustomException(INVALID_FILE_TYPE, "적절하지 않은 파일 유형: 압축파일");
            }
        } catch (IOException e) {
            log.error("{}: {}",INVALID_FILE_TYPE.getMessage(), "ZIP");
            throw new CustomException(INVALID_FILE_TYPE, "적절하지 않은 파일 유형: 압축파일");
        }
    }

    public static boolean isExtractable(Path path) {
        int fileSignature;
        try (RandomAccessFile raf = new RandomAccessFile(path.toString(), "r")) {
            fileSignature = raf.readInt();
            return fileSignature == 0x504B0304 || fileSignature == 0x504B0506 || fileSignature == 0x504B0708;
        } catch (IOException e) {
            log.error("{}: {}",INVALID_FILE_TYPE.getMessage(), "ZIP");
            throw new CustomException(INVALID_FILE_TYPE, "적절하지 않은 파일 유형: 압축파일");
        }
    }

    public static String getExtension(Path path) {
        return getExtension(path.toString());
    }

    public static String getExtension(String fileName) {
        return Optional.of(fileName)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(fileName.lastIndexOf(".") + 1))
                .orElseThrow();
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

    public static Path createTempFile(String suffix) throws IOException {
        return Files.createTempFile(PREFIX, suffix);
    }

    public static Path createTempDirectory(String suffix) throws IOException {
        return Files.createTempDirectory(PREFIX + suffix);
    }

}

