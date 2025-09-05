package com.pluxity.global.utils

import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.UUID

object FileUtils {
    fun saveFileToDirectory(
        multipartFile: MultipartFile,
        targetDirectory: Path,
    ): Path {
        val targetPath = Paths.get(targetDirectory.toString(), "${UUID.randomUUID()}_${multipartFile.originalFilename}")
        Files.createDirectories(targetPath.parent)

        BufferedInputStream(multipartFile.inputStream).use { inputStream ->
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)
        }

        return targetPath
    }

    fun deleteDirectoryRecursively(dir: Path) {
        Files.walkFileTree(
            dir,
            object : SimpleFileVisitor<Path>() {
                override fun visitFile(
                    file: Path,
                    attrs: BasicFileAttributes,
                ): FileVisitResult {
                    Files.delete(file)
                    return FileVisitResult.CONTINUE
                }

                override fun postVisitDirectory(
                    directory: Path,
                    exc: IOException?,
                ): FileVisitResult {
                    if (exc != null) throw exc
                    Files.delete(directory)
                    return FileVisitResult.CONTINUE
                }
            },
        )
    }

    fun createTempFile(originalFileName: String): Path {
        val tempDir = System.getProperty("java.io.tmpdir")
        val tempFilePath = Paths.get(tempDir, "${UUID.randomUUID()}_$originalFileName")
        Files.createDirectories(tempFilePath.parent)
        return tempFilePath
    }

    fun createTempDirectory(suffix: String): Path = Files.createTempDirectory("pluxity-$suffix")

    fun getContentType(multipartFile: MultipartFile): String = multipartFile.contentType ?: "application/octet-stream"

    fun getFileExtension(fileName: String): String {
        val lastDotIndex = fileName.lastIndexOf('.')
        return if (lastDotIndex > 0) fileName.substring(lastDotIndex) else ""
    }
}
