package com.pluxity.global.utils;

import org.springframework.data.domain.Sort;

public class SortUtils {

    public static Sort getOrderByCreatedAtDesc() {
        return Sort.by(Sort.Direction.DESC, "CreatedAt");
    }
}
