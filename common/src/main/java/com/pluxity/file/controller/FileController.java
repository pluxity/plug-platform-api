package com.pluxity.file.controller;

import com.pluxity.file.constant.FileType;
import com.pluxity.file.dto.UploadResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/pre-signed-url")
    public ResponseEntity<String> getPreSignedUrl(@RequestParam String s3Key) {
        String url = fileService.generatePreSignedUrl(s3Key);
        return ResponseEntity.ok(url);
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file,
                                                     @RequestParam(value = "type", required = false, defaultValue = "DEFAULT") FileType type) {
        return ResponseEntity.ok(fileService.initiateUpload(file, type));
    }
}
