package com.pluxity.file.strategy.process;

import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;

public interface FileProcessingStrategy {
    Path process(MultipartFile multipartFile) throws Exception;
}
