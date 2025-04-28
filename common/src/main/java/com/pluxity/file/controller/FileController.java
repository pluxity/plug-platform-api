package com.pluxity.file.controller;

import com.pluxity.file.constant.FileType;
import com.pluxity.file.dto.FileUploadResponse;
import com.pluxity.file.dto.UploadResponse;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.file.service.FileService;
import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.response.CreatedResponseBody;
import com.pluxity.global.response.DataResponseBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @GetMapping("/pre-signed-url")
    public ResponseEntity<String> getPreSignedUrl(@RequestParam String s3Key) {
        String url = fileService.generatePreSignedUrl(s3Key);
        return ResponseEntity.ok(url);
    }

    @PostMapping("/upload")
    @ResponseCreated
    public ResponseEntity<CreatedResponseBody<Long>> uploadFile(@RequestParam("file") MultipartFile file,
                                                                      @RequestParam(value = "type", required = false, defaultValue = "DEFAULT") FileType type) {
//        fileService.initiateUpload(file, type);
        return ResponseEntity.ok(CreatedResponseBody.of(fileService.initiateUpload(file, type).id()));
    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<FileUploadResponse> getFileInfo(@PathVariable Long id) {
//        FileUploadResponse file = fileService.getFile(id);
//
//
//    }
}
