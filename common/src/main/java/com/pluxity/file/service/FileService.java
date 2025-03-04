package com.pluxity.file.service;

import com.pluxity.file.constant.FileStatus;
import com.pluxity.file.constant.FileType;
import com.pluxity.file.dto.FileUploadResponse;
import com.pluxity.file.dto.UploadResponse;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.file.repository.FileRepository;
import com.pluxity.file.strategy.storage.FilePersistenceContext;
import com.pluxity.file.strategy.storage.FileProcessingContext;
import com.pluxity.file.strategy.storage.StorageStrategy;
import com.pluxity.global.config.S3Config;
import com.pluxity.global.exception.CustomException;
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

import java.net.URL;
import java.time.Duration;

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
    public UploadResponse initiateUpload(MultipartFile multipartFile, FileType fileType) {

        try {
            FileProcessingContext context = storageStrategy.save(multipartFile);

            FileEntity fileEntity = FileEntity.builder()
                    .fileType(fileType)
                    .filePath(context.savedPath())
                    .originalFileName(context.originalFileName())
                    .contentType(context.contentType())
                    .build();

            repository.save(fileEntity);

            if (fileType.equals(FileType.SBM)) {
                return sbmFileService.processSbmFile(context.originalFilePath(), fileEntity);
            }

            return FileUploadResponse.from(fileEntity);

        } catch (Exception e) {
            log.error("File Save Exception : {}",e.getMessage());
            throw new CustomException(INVALID_FILE_TYPE, e.getMessage());
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
}
