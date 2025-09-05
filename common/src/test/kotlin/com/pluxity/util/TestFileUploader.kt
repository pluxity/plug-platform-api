package com.pluxity.util

import com.pluxity.file.service.FileService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockMultipartFile
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class TestFileUploader {
    @Autowired
    lateinit var fileService: FileService

    fun initiateTestFileUpload(filename: String): Long {
        val mockFile: MultipartFile =
            MockMultipartFile(
                "file", // 파라미터 이름
                filename, // 원본 파일명
                "image/png", // 컨텐츠 타입
                "test-image-content".toByteArray(), // 파일 내용
            )
        return fileService.initiateUpload(mockFile)
    }
}
