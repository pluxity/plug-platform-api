package com.pluxity.file.strategy.storage;

import com.pluxity.global.config.S3Config;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.FileUtils;
import com.pluxity.global.utils.ZipUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.stream.Stream;

import static com.pluxity.global.constant.ErrorCode.FAILED_TO_UPLOAD_FILE;
import static com.pluxity.global.constant.ErrorCode.FAILED_TO_ZIP_FILE;

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

    @Override
    public String persist(FilePersistenceContext context) throws Exception {

        String oldKey = context.filePath();
        String persistKey = oldKey.replace("temp/", context.newPath());

        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(s3Config.getBucketName())
                .sourceKey(oldKey)
                .destinationBucket(s3Config.getBucketName())
                .destinationKey(persistKey)
                .build();
        s3Client.copyObject(copyRequest);

        if (context.contentType().equalsIgnoreCase("application/zip") || persistKey.endsWith(".zip")) {
            decompressAndUpload(persistKey);
        }

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(oldKey)
                .build();

        s3Client.deleteObject(deleteRequest);

        return persistKey;
    }



    private void decompressAndUpload(String persistKey) {
        Path tempZipFilePath = null;
        Path tempDir = null;
        try {
            tempZipFilePath = FileUtils.createTempFile(".zip");

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(persistKey)
                    .build();

            try (InputStream s3ObjectContent = s3Client.getObject(getObjectRequest);
                 OutputStream outputStream = Files.newOutputStream(tempZipFilePath,
                         StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = s3ObjectContent.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            tempDir = FileUtils.createTempDirectory("unzipped");
            try (InputStream is = Files.newInputStream(tempZipFilePath)) {
                ZipUtils.unzip(is, tempDir);
            }

            String baseFolder = persistKey.replace(".zip", "");
            uploadDirectoryToS3(tempDir, baseFolder);

        } catch (Exception e) {
            log.error("압축 파일 처리 중 오류 발생 (zipKey: {}): {}", persistKey, e.getMessage(), e);
            throw new CustomException(FAILED_TO_ZIP_FILE, "압축 파일 처리 중 오류 발생");
        } finally {
            if (tempZipFilePath != null) {
                try {
                    Files.deleteIfExists(tempZipFilePath);
                } catch (IOException ex) {
                    log.warn("임시 ZIP 파일 삭제 실패: {}", tempZipFilePath, ex);
                }
            }

            if (tempDir != null) {
                try {
                    FileUtils.deleteDirectoryRecursively(tempDir);
                } catch (IOException ex) {
                    log.warn("임시 디렉토리 삭제 실패: {}", tempDir, ex);
                }
            }
        }
    }


    private void uploadDirectoryToS3(Path dir, String s3BaseKey) {
        try (Stream<Path> paths = Files.walk(dir)) { // try-with-resources 사용
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            String relativePath = dir.relativize(path).toString().replace("\\", "/");
                            String key = s3BaseKey + "/" + relativePath;
                            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                    .bucket(s3Config.getBucketName())
                                    .key(key)
                                    .build();
                            s3Client.putObject(putObjectRequest, RequestBody.fromFile(path.toFile()));
                        } catch (Exception e) {
                            log.error("Failed to upload file {}: {}", path, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            log.error("압축 해제 된 파일 업로드 실패 {}: {}", dir, e.getMessage());
            throw new CustomException(FAILED_TO_UPLOAD_FILE);
        }
    }
}
