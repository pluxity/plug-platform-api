package com.pluxity.facility.path

import com.pluxity.global.constant.ErrorCode.NOT_FOUND_PATH_TYPE
import com.pluxity.global.exception.CustomException

enum class PathType(
    val displayName: String,
) {
    SUBWAY("지하철"),
    WAY("길찾기"),
    PATROL("순찰"),
    ;

    companion object {
        fun from(type: String): PathType =
            entries.find { it.name.equals(type, ignoreCase = true) }
                ?: throw CustomException(NOT_FOUND_PATH_TYPE, type)
    }
}
