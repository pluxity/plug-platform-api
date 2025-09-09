package com.pluxity.global.exception

import com.pluxity.global.constant.ErrorCode

class CustomException(
    val errorCode: ErrorCode,
    @Transient vararg val params: Any?,
) : RuntimeException(formatMessage(errorCode, params)) {
    companion object {
        private fun formatMessage(
            errorCode: ErrorCode,
            params: Array<out Any?>,
        ): String {
            if (params.isEmpty()) return errorCode.getMessage()
            return try {
                String.format(errorCode.getMessage(), *params)
            } catch (_: Exception) {
                "${errorCode.getMessage()} (format params: ${params.contentToString()})"
            }
        }
    }
}
