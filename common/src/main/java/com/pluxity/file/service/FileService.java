package com.pluxity.file.service;

import com.pluxity.file.constant.FileStatus;
import com.pluxity.file.constant.FileType;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.dto.FileUploadResponse;
import com.pluxity.file.dto.UploadResponse;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.file.repository.FileRepository;
import com.pluxity.file.strategy.storage.FilePersistenceContext;
import com.pluxity.file.strategy.storage.FileProcessingContext;
import com.pluxity.file.strategy.storage.StorageStrategy;
import com.pluxity.global.config.S3Config;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

import static com.pluxity.global.constant.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final S3Presigner s3Presigner;
    private final S3Config s3Config;
    private final StorageStrategy storageStrategy;
    private final FileRepository repository;
    private final SbmFileService sbmFileService;

    @Value("${file.storage-strategy}")
    private String storageStrategyType;

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
    public UploadResponse initiateUpload(MultipartFile file, FileType type) {
        try {
            // 임시 파일로 저장
            Path tempPath = FileUtils.createTempFile(file.getOriginalFilename());
            file.transferTo(tempPath);

            // 파일 컨텍스트 생성
            var context = FileProcessingContext.builder()
                    .contentType(FileUtils.getContentType(file))
                    .tempPath(tempPath)
                    .originalFileName(file.getOriginalFilename())
                    .build();

            // 스토리지에 저장
            String filePath = storageStrategy.save(context);

            // 엔티티 생성 및 저장
            FileEntity fileEntity = FileEntity.builder()
                    .filePath(filePath)
                    .originalFileName(file.getOriginalFilename())
                    .contentType(FileUtils.getContentType(file))
                    .fileType(type)
                    .build();

            FileEntity savedFile = repository.save(fileEntity);

            // 임시 파일 삭제
            Files.deleteIfExists(tempPath);

            if (type == FileType.SBM) {
                return sbmFileService.processSbmFile(savedFile);
            }

            return FileUploadResponse.from(savedFile);
        } catch (Exception e) {
            log.error("File Upload Exception : {}", e.getMessage(), e);
            throw new CustomException(FAILED_TO_UPLOAD_FILE, e.getMessage());
        }
    }

    @Transactional
    public FileEntity finalizeUpload(Long fileId, String newPath) {

        try {
            FileEntity file = repository.findById(fileId)
                    .orElseThrow(() -> new CustomException("File not found", HttpStatus.NOT_FOUND, "해당 파일 아이디를 찾지 못했습니다"));

            if (file.getFileStatus() != FileStatus.TEMP) {
                throw new CustomException(INVALID_FILE_STATUS, "임시 파일이 아닌 경우에는 영구 저장할 수 없습니다");
            }

            var context = FilePersistenceContext.builder()
                    .filePath(file.getFilePath())
                    .newPath(newPath)
                    .contentType(file.getContentType())
                    .originalFileName(file.getOriginalFileName())
                    .build();

            String persistPath = storageStrategy.persist(context);

            file.makeComplete(persistPath);
            return file;

        } catch (Exception e) {
            log.error("File Persist Exception : {}", e.getMessage());
            throw new CustomException(INVALID_FILE_STATUS, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public FileEntity getFile(Long fileId) {
        return repository.findById(fileId)
                .orElseThrow(() -> new CustomException("File not found", HttpStatus.NOT_FOUND, "해당 파일을 찾을 수 없습니다"));
    }

    @Transactional(readOnly = true)
    public FileResponse getFileResponse(Long fileId) {

        if (fileId == null) {
            return null;
        }

        return getFileResponse(getFile(fileId));
    }

    public FileResponse getFileResponse(FileEntity fileEntity) {

        String url = "local".equals(storageStrategyType) ?
                "/files/" + fileEntity.getFilePath() :
                this.generatePreSignedUrl(fileEntity.getFilePath());

        return FileResponse.builder()
                .id(fileEntity.getId())
                .url(url)
                .originalFileName(fileEntity.getOriginalFileName())
                .fileType(fileEntity.getFileType())
                .contentType(fileEntity.getContentType())
                .fileStatus(fileEntity.getFileStatus().name())
                .createdAt(fileEntity.getCreatedAt())
                .updatedAt(fileEntity.getUpdatedAt())
                .build();
    }
}
