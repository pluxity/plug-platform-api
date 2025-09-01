package com.pluxity.file.service

import com.pluxity.file.constant.FileStatus
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.dto.toFileResponse
import com.pluxity.file.entity.FileEntity
import com.pluxity.file.repository.FileRepository
import com.pluxity.file.strategy.storage.FilePersistenceContext
import com.pluxity.file.strategy.storage.FileProcessingContext
import com.pluxity.file.strategy.storage.StorageStrategy
import com.pluxity.global.config.S3Config
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.FileUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.nio.file.Files
import java.time.Duration

private val log = KotlinLogging.logger {}

@Service
class FileService(
    private val s3Presigner: S3Presigner,
    private val s3Config: S3Config,
    private val storageStrategy: StorageStrategy,
    private val repository: FileRepository,
) {
    @Value("\${file.storage-strategy}")
    lateinit var storageStrategyType: String

    @Value("\${file.s3.bucket}")
    lateinit var bucket: String

    @Value("\${file.s3.public-url}")
    lateinit var publicUrl: String

    // TODO: PreSigned URL 생성 시 추가 로직 필요 (예: Drawing / ID 등)
    fun generatePreSignedUrl(s3Key: String): String {
        val getObjectRequest =
            GetObjectRequest
                .builder()
                .bucket(s3Config.bucketName)
                .key(s3Key)
                .build()

        val presignRequest =
            GetObjectPresignRequest
                .builder()
                .signatureDuration(Duration.ofSeconds(s3Config.preSignedUrlExpiration.toLong()))
                .getObjectRequest(getObjectRequest)
                .build()

        val preSignedUrl = s3Presigner.presignGetObject(presignRequest).url()
        return preSignedUrl.toString()
    }

    @Transactional
    fun initiateUpload(file: MultipartFile): Long {
        try {
            // 임시 파일로 저장
            val tempPath = FileUtils.createTempFile(file.originalFilename)
            file.transferTo(tempPath)

            // 파일 컨텍스트 생성
            val context =
                FileProcessingContext(
                    contentType = FileUtils.getContentType(file),
                    tempPath = tempPath,
                    originalFileName = file.originalFilename!!,
                )

            // 스토리지에 저장
            val filePath = storageStrategy.save(context)

            // 엔티티 생성 및 저장
            val fileEntity =
                FileEntity(
                    filePath = filePath,
                    originalFileName = file.originalFilename!!,
                    contentType = FileUtils.getContentType(file),
                )

            val savedFile = repository.save<FileEntity>(fileEntity)

            // 임시 파일 삭제
            Files.deleteIfExists(tempPath)
            return savedFile.id!!
        } catch (e: Exception) {
            log.error { "File Upload Exception : ${e.message}" }
            throw CustomException(ErrorCode.FAILED_TO_UPLOAD_FILE, e.message)
        }
    }

    @Transactional
    fun finalizeUpload(
        fileId: Long,
        newPath: String,
    ): FileEntity {
        try {
            val file =
                repository
                    .findByIdOrNull(fileId)
                    ?: throw CustomException(ErrorCode.NOT_FOUND_FILE, fileId)

            require(file.fileStatus == FileStatus.TEMP) {
                throw CustomException(ErrorCode.INVALID_FILE_STATUS, "임시 파일이 아닌 경우에는 영구 저장할 수 없습니다")
            }

            val context =
                FilePersistenceContext(
                    filePath = file.filePath,
                    newPath = newPath,
                    contentType = file.contentType,
                    originalFileName = file.originalFileName,
                )

            val persistPath = storageStrategy.persist(context)

            file.makeComplete(persistPath)
            return file
        } catch (e: Exception) {
            log.error { "File Persist Exception : $e.message" }
            throw CustomException(ErrorCode.INVALID_FILE_STATUS, e.message)
        }
    }

    @Transactional(readOnly = true)
    fun getFile(fileId: Long): FileEntity =
        repository
            .findByIdOrNull(fileId)
            ?: throw CustomException(ErrorCode.NOT_FOUND_FILE, fileId)

    @Transactional(readOnly = true)
    fun getFiles(fileIds: List<Long>): List<FileResponse> =
        fileIds
            .takeIf { it.isNotEmpty() }
            ?.let { fileId -> repository.findByIdIn(fileId).mapNotNull { getFileResponse(it) } }
            ?: emptyList()

    @Transactional(readOnly = true)
    fun getFileResponse(fileId: Long?): FileResponse? =
        fileId?.let { id ->
            getFileResponse(getFile(id))
        }

    fun getFileResponse(fileEntity: FileEntity?): FileResponse? =
        fileEntity?.let { file ->
            val url =
                if ("local" == storageStrategyType) {
                    "/files/${file.filePath}"
                } else {
                    "$publicUrl/$bucket/${file.filePath}"
                }
            file.toFileResponse(url)
        }
}
