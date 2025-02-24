package com.pluxity.file.strategy;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;


public interface FileProcessingStrategy {
    Path process(MultipartFile multipartFile) throws Exception;
}
