package com.pluxity.file.dto;

public interface UploadResponse {
    Long id();
    String filePath();
    String originalFileName();
    String contentType();
    String createdAt();
}
