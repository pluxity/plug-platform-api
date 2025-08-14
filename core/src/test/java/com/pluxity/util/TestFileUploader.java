package com.pluxity.util;

import com.pluxity.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class TestFileUploader {

    private final FileService fileService;

    public Long initiateTestFileUpload(String filename) {
        MultipartFile mockFile =
                new MockMultipartFile(
                        "file", // 파라미터 이름
                        filename, // 원본 파일명
                        "image/png", // 컨텐츠 타입
                        "test-image-content".getBytes() // 파일 내용
                        );
        return fileService.initiateUpload(mockFile);
    }
}
