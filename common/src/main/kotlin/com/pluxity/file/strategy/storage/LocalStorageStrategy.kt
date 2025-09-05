package com.pluxity.file.strategy.storage

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.FileUtils
import com.pluxity.global.utils.UUIDUtils
import com.pluxity.global.utils.ZipUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

private val log = KotlinLogging.logger {}

class LocalStorageStrategy : StorageStrategy {
    @Value("\${file.local.path}")
    private lateinit var uploadPath: String

    override fun save(context: FileProcessingContext): String {
        try {
            val uniqueFileName = UUIDUtils.generateUUID()
            val fileExtension = FileUtils.getFileExtension(context.originalFileName)
            val fileName = uniqueFileName + fileExtension

            // 임시저장을 위한 temp 디렉토리 경로 (uploadPath/temp)
            val tempDir = Paths.get(uploadPath, "temp")
            Files.createDirectories(tempDir)
            val filePath = tempDir.resolve(fileName)

            Files.copy(context.tempPath, filePath, StandardCopyOption.REPLACE_EXISTING)

            return "temp/$fileName"
        } catch (e: Exception) {
            log.error { "Failed to save file: ${e.message}" }
            throw CustomException(ErrorCode.FAILED_TO_UPLOAD_FILE)
        }
    }

    override fun persist(context: FilePersistenceContext): String {
        try {
            val sourcePath = Paths.get(uploadPath, context.filePath)
            val targetDir = Paths.get(uploadPath, context.newPath)

            val fileName = UUIDUtils.generateShortUUID()
            val extension = FileUtils.getFileExtension(context.originalFileName)

            val targetPath = targetDir.resolve(fileName + extension)

            Files.createDirectories(targetDir)
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)

            return "${context.newPath}${targetPath.fileName}"
        } catch (e: Exception) {
            log.error { "Failed to persist file: ${e.message}" }
            throw CustomException(ErrorCode.FAILED_TO_UPLOAD_FILE)
        }
    }

    private fun decompressAndMove(
        zipFilePath: Path,
        baseDirPath: Path,
    ) {
        var tempDir: Path? = null
        try {
            tempDir = FileUtils.createTempDirectory("unzipped")
            Files.newInputStream(zipFilePath).use { `is` ->
                ZipUtils.unzip(`is`, tempDir)
            }
            moveDirectory(tempDir, baseDirPath)
        } catch (e: Exception) {
            log.error(e) { "압축 파일 처리 중 오류 발생 (zipPath: $zipFilePath): ${e.message}" }
            throw CustomException(ErrorCode.FAILED_TO_ZIP_FILE, "압축 파일 처리 중 오류 발생")
        } finally {
            if (tempDir != null) {
                try {
                    FileUtils.deleteDirectoryRecursively(tempDir)
                } catch (ex: IOException) {
                    log.warn(ex) { "임시 디렉토리 삭제 실패: $tempDir" }
                }
            }
        }
    }

    private fun moveDirectory(
        sourceDir: Path,
        targetDir: Path,
    ) {
        Files.walk(sourceDir).use { paths ->
            paths
                .filter { Files.isRegularFile(it) }
                .forEach { sourcePath ->
                    try {
                        val relativePath = sourceDir.relativize(sourcePath)
                        val targetPath = targetDir.resolve(relativePath)
                        Files.createDirectories(targetPath.parent)
                        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)
                    } catch (e: Exception) {
                        log.error { "Failed to move file $sourcePath: ${e.message}" }
                        throw CustomException(ErrorCode.FAILED_TO_UPLOAD_FILE, "압축 파일 이동 중 오류 발생")
                    }
                }
        }
    }
}
