package com.pluxity.global.utils;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@Slf4j
public class UUIDUtils {

    public static String generateShortUUID() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
    }
}
