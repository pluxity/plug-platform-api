package com.pluxity.global.utils

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.multipart.MultipartFile
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private val log = KotlinLogging.logger {}

object ZipUtils {
    fun zip(
        file: MultipartFile,
        bp: Path,
    ) {
        runCatching {
            FileOutputStream(bp.toFile()).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    file.originalFilename?.let { filename ->
                        ZipEntry(filename).also { entry ->
                            zos.putNextEntry(entry)
                            zos.write(file.bytes)
                            zos.closeEntry()
                        }
                    }
                }
            }
        }.getOrElse { e ->
            log.error { "압축 실패: ${e.message}" }
            throw CustomException(ErrorCode.FAILED_TO_ZIP_FILE)
        }
    }

    fun unzip(
        inputStream: InputStream,
        targetDirectory: Path,
    ) {
        val destDirPath = targetDirectory.toAbsolutePath().normalize()

        ZipInputStream(inputStream, StandardCharsets.UTF_8).use { zis ->
            generateSequence { zis.nextEntry }
                .forEach { entry ->
                    val targetFilePath = destDirPath.resolve(entry.name).normalize()
                    require(targetFilePath.startsWith(destDirPath)) {
                        "ZIP 엔트리가 대상 폴더 외부에 위치합니다: ${entry.name}"
                    }

                    when {
                        entry.isDirectory -> Files.createDirectories(targetFilePath)
                        else -> {
                            Files.createDirectories(targetFilePath.parent)
                            Files.copy(zis, targetFilePath, StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
                    zis.closeEntry()
                }
        }
    }
}
