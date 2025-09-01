package com.pluxity.file.strategy.storage

import com.pluxity.global.config.S3Config
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.FileUtils
import com.pluxity.global.utils.UUIDUtils
import com.pluxity.global.utils.ZipUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.UUID

private val log = KotlinLogging.logger {}

class S3StorageStrategy(
    private val s3Config: S3Config,
    private val s3Client: S3Client,
) : StorageStrategy {
    @Throws(Exception::class)
    override fun save(context: FileProcessingContext): String {
        val s3Key =
            "temp/${UUID.randomUUID()}/${UUIDUtils.generateShortUUID()}${FileUtils.getFileExtension(context.originalFileName)}"

        val putObjectRequest =
            PutObjectRequest
                .builder()
                .bucket(s3Config.bucketName)
                .key(s3Key)
                .contentType(context.contentType)
                .build()

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(context.tempPath.toFile()))

        return s3Key
    }

    override fun persist(context: FilePersistenceContext): String {
        val oldKey = context.filePath
        val persistKey = oldKey.replace("temp/", context.newPath)

        val copyRequest =
            CopyObjectRequest
                .builder()
                .sourceBucket(s3Config.bucketName)
                .sourceKey(oldKey)
                .destinationBucket(s3Config.bucketName)
                .destinationKey(persistKey)
                .build()
        s3Client.copyObject(copyRequest)

        if (context.contentType.equals("application/zip", ignoreCase = true) || persistKey.endsWith(".zip")) {
            decompressAndUpload(persistKey)
        }

        val deleteRequest =
            DeleteObjectRequest
                .builder()
                .bucket(s3Config.bucketName)
                .key(oldKey)
                .build()

        s3Client.deleteObject(deleteRequest)

        return persistKey
    }

    private fun decompressAndUpload(persistKey: String) {
        var tempZipFilePath: Path? = null
        var tempDir: Path? = null
        try {
            tempZipFilePath = FileUtils.createTempFile(".zip")

            val getObjectRequest =
                GetObjectRequest
                    .builder()
                    .bucket(s3Config.bucketName)
                    .key(persistKey)
                    .build()

            s3Client.getObject(getObjectRequest).use { s3ObjectContent ->
                Files
                    .newOutputStream(
                        tempZipFilePath,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                    ).use { outputStream ->
                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        while ((s3ObjectContent.read(buffer).also { bytesRead = it }) != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                    }
            }
            tempDir = FileUtils.createTempDirectory("unzipped")
            Files.newInputStream(tempZipFilePath).use { `is` ->
                ZipUtils.unzip(`is`, tempDir)
            }
            val lastSlashIndex = persistKey.lastIndexOf('/')
            val baseFolder =
                if (lastSlashIndex != -1) persistKey.take(lastSlashIndex) else persistKey
            uploadDirectoryToS3(tempDir, baseFolder)
        } catch (e: Exception) {
            log.error { "압축 파일 처리 중 오류 발생 (zipKey: $persistKey): $e.message" }
            throw CustomException(ErrorCode.FAILED_TO_ZIP_FILE, "압축 파일 처리 중 오류 발생")
        } finally {
            if (tempZipFilePath != null) {
                try {
                    Files.deleteIfExists(tempZipFilePath)
                } catch (ex: IOException) {
                    log.warn(ex) { "임시 ZIP 파일 삭제 실패: $tempZipFilePath" }
                }
            }

            if (tempDir != null) {
                try {
                    FileUtils.deleteDirectoryRecursively(tempDir)
                } catch (ex: IOException) {
                    log.warn(ex) { "임시 디렉토리 삭제 실패: $tempDir" }
                }
            }
        }
    }

    private fun uploadDirectoryToS3(
        dir: Path,
        s3BaseKey: String,
    ) {
        try {
            Files.walk(dir).use { paths ->
                // try-with-resources 사용
                paths
                    .filter { path: Path -> Files.isRegularFile(path) }
                    .forEach { path: Path ->
                        try {
                            val relativePath = dir.relativize(path).toString().replace("\\", "/")
                            val key = "$s3BaseKey/$relativePath"
                            val putObjectRequest =
                                PutObjectRequest
                                    .builder()
                                    .bucket(s3Config.bucketName)
                                    .key(key)
                                    .build()
                            s3Client.putObject(putObjectRequest, RequestBody.fromFile(path.toFile()))
                        } catch (e: Exception) {
                            log.error(e) { "Failed to upload file $path: $e.message" }
                        }
                    }
            }
        } catch (e: IOException) {
            log.error { "압축 해제 된 파일 업로드 실패 $dir: $e.message" }
            throw CustomException(ErrorCode.FAILED_TO_UPLOAD_FILE)
        }
    }
}
