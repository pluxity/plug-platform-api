package com.pluxity.file.strategy;

import com.pluxity.global.utils.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;

@Component("SIMPLE_STRATEGY")
public class SimpleFileStrategy implements FileProcessingStrategy {

    @Override
    public Path process(MultipartFile multipartFile) throws Exception {
        Path tempFile = FileUtils.createTempFile(multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);
        return tempFile;
    }
}
