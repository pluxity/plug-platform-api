package com.pluxity.category.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryType {
    FACILITY(3),
    ASSET(2),
    DEFAULT(5);

    private final int maxDepth;
} 