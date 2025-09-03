package com.pluxity.global.utils

import java.util.UUID

object UUIDUtils {
    fun generateUUID(): String = UUID.randomUUID().toString()

    fun generateShortUUID(): String = generateUUID().take(8)
}
