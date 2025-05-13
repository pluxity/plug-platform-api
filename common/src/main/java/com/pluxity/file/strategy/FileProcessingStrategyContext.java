package com.pluxity.file.strategy;

import com.pluxity.file.strategy.process.FileProcessingStrategy;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileProcessingStrategyContext {

    private final Map<String, FileProcessingStrategy> strategyMap;

    @Autowired
    public FileProcessingStrategyContext(Map<String, FileProcessingStrategy> strategyMap) {
        this.strategyMap = strategyMap;
    }
}
