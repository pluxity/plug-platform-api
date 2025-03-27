package com.pluxity.global.utils;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@Slf4j
public class UUIDUtils {

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String generateShortUUID() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
