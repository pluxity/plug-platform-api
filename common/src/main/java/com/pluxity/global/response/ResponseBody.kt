package com.pluxity.global.response

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

open class ResponseBody(
    val status: Int,
    val message: String?,
) {
    val timestamp: String = LocalDateTime.now().format(FORMATTER)

    companion object {
        private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}
