package com.pluxity.file.dto;

public interface UploadResponse {
    Long id();
    String originalFileName();
    String filePath();
    String type();
    String createdAt();
}
