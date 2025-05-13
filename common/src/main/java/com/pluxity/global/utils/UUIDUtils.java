package com.pluxity.global.utils;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UUIDUtils {

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String generateShortUUID() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
