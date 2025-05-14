package com.pluxity.file.strategy;

import com.pluxity.file.strategy.process.FileProcessingStrategy;
import com.pluxity.global.utils.FileUtils;
import java.nio.file.Path;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component("SIMPLE_STRATEGY")
public class SimpleFileStrategy implements FileProcessingStrategy {

    @Override
    public Path process(MultipartFile multipartFile) throws Exception {
        Path tempFile = FileUtils.createTempFile(multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);
        return tempFile;
    }
}
