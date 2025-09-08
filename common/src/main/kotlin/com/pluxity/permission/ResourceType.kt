package com.pluxity.permission

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException

enum class ResourceType(
    val resourceName: String,
    val endpoint: String,
) {
    NONE("NONE", ""),
    FACILITY("시설", "facilities"),
    DEVICE_CATEGORY("장비 분류", "device-categories"),
    CCTV("CCTV", "cctvs"),
    ;

    companion object {
        fun fromString(resourceName: String): ResourceType =
            entries.firstOrNull { it != NONE && it.name.equals(resourceName, ignoreCase = true) }
                ?: throw CustomException(ErrorCode.INVALID_RESOURCE_TYPE, "Resource type: $resourceName")
    }
}
