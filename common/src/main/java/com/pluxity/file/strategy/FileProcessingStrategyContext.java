package com.pluxity.file.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FileProcessingStrategyContext {

    private final Map<String, FileProcessingStrategy> strategyMap;

    @Autowired
    public FileProcessingStrategyContext(Map<String, FileProcessingStrategy> strategyMap) {
        this.strategyMap = strategyMap;
    }
}