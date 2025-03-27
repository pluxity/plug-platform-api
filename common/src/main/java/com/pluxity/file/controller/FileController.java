package com.pluxity.file.controller;

import com.pluxity.file.constant.FileType;
import com.pluxity.file.dto.UploadResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
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
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file,
                                                     @RequestParam(value = "type", required = false, defaultValue = "DEFAULT") FileType type) {
        return ResponseEntity.ok(fileService.initiateUpload(file, type));
    }
    
    /**
     * 파일 다운로드 엔드포인트
     * 네트워크 마운트된 파일 시스템에서 파일을 가져와 다운로드합니다.
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            Resource resource = fileService.getFileResource(fileId);
            String encodedFilename = URLEncoder.encode(resource.getFilename(), StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            log.error("파일 다운로드 중 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.FILE_ACCESS_ERROR, "파일 다운로드 중 오류 발생: " + e.getMessage());
        }
    }
    
    /**
     * 파일 존재 여부 확인 엔드포인트
     */
    @GetMapping("/exists/{fileId}")
    public ResponseEntity<Boolean> checkFileExists(@PathVariable Long fileId) {
        return ResponseEntity.ok(fileService.isFileExists(fileId));
    }
    
    /**
     * 파일 완료 처리 엔드포인트
     * 임시 파일을 지정된 경로로 저장합니다.
     */
    @PostMapping("/{fileId}/finalize")
    public ResponseEntity<Void> finalizeFile(
            @PathVariable Long fileId,
            @RequestParam("path") String path) {
        fileService.finalizeUpload(fileId, path);
        return ResponseEntity.ok().build();
    }
}
