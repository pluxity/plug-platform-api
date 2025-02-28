package com.pluxity.file.service;

import com.pluxity.file.constant.FileStatus;
import com.pluxity.file.constant.FileType;
import com.pluxity.file.dto.FileUploadResponse;
import com.pluxity.file.dto.UploadResponse;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.file.repository.FileRepository;
import com.pluxity.file.strategy.storage.FileProcessingContext;
import com.pluxity.file.strategy.storage.StorageStrategy;
import com.pluxity.global.config.S3Config;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.FileUtils;
import com.pluxity.global.utils.ZipUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

import static com.pluxity.global.constant.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final S3Presigner s3Presigner;
    private final S3Config s3Config;
    private final S3Client s3Client;

    private final StorageStrategy storageStrategy;

    private final FileRepository repository;

    private final SbmFileService sbmFileService;

    // TODO: PreSigned URL 생성 시 추가 로직 필요 (예: Drawing / ID 등)
    public String generatePreSignedUrl(String s3Key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(s3Config.getPreSignedUrlExpiration()))
                .getObjectRequest(getObjectRequest)
                .build();

        URL preSignedUrl = s3Presigner.presignGetObject(presignRequest).url();
        return preSignedUrl.toString();
    }

    @Transactional
    public UploadResponse initiateUpload(MultipartFile multipartFile, FileType type) throws Exception {

        FileProcessingContext context = storageStrategy.save(multipartFile, type);
        Path tempPath = FileUtils.createTempFile(multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempPath.toFile());

        String originalFileName = multipartFile.getOriginalFilename();
        String fileType = Optional.ofNullable(multipartFile.getContentType())
                .orElseGet(() -> {
                    try {
                        return Files.probeContentType(tempPath);
                    } catch (IOException e) {
                        return "application/octet-stream";
                    }
                });

        String s3Key = "temp/" + UUID.randomUUID() + "-" + originalFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(s3Key)
                .contentType(fileType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(tempPath.toFile()));

        var fileEntity = FileEntity.builder()
                .fileType(fileType)
                .originalFileName(originalFileName)
                .filePath(s3Key)
                .build();

        repository.save(fileEntity);

        if (isSbm) {
            return sbmFileService.processSbmFile(tempPath, fileEntity);
        }

        try {
            Files.deleteIfExists(tempPath);
        } catch (IOException e) {
            log.warn("Failed to delete temp file: {}", tempPath);
        }

        return FileUploadResponse.from(fileEntity);
    }

    @Transactional
    public FileEntity finalizeUpload(Long fileId, String newKey) throws IOException {
        FileEntity file = repository.findById(fileId)
                .orElseThrow(() -> new CustomException("File not found", HttpStatus.NOT_FOUND, "해당 파일 아이디를 찾지 못했습니다"));

        if (file.getFileStatus() != FileStatus.TEMP) {
            throw new CustomException(INVALID_FILE_STATUS, "임시 파일이 아닌 경우에는 영구 저장할 수 없습니다");
        }

        String oldKey = file.getFilePath();
        String permanentKey = oldKey.replace("temp/", newKey);

        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(s3Config.getBucketName())
                .sourceKey(oldKey)
                .destinationBucket(s3Config.getBucketName())
                .destinationKey(permanentKey)
                .build();
        s3Client.copyObject(copyRequest);

        file.makeComplete(permanentKey);

        if (file.getFileType().equalsIgnoreCase("application/zip") || permanentKey.endsWith(".zip")) {
            decompressAndUpload(permanentKey);
        }

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(oldKey)
                .build();
        s3Client.deleteObject(deleteRequest);

        return file;
    }

    private void decompressAndUpload(String permanentKey) {
        Path tempZipFilePath = null;
        Path tempDir = null;
        try {
            tempZipFilePath = FileUtils.createTempFile(".zip");

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(permanentKey)
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

            String baseFolder = permanentKey.replace(".zip", "");
            uploadDirectoryToS3(tempDir, baseFolder);

        } catch (Exception e) {
            log.error("압축 파일 처리 중 오류 발생 (zipKey: {}): {}", permanentKey, e.getMessage(), e);
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
