package com.pluxity.facility.path

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import lombok.Getter
import lombok.RequiredArgsConstructor
import java.util.*
import java.util.function.Supplier

@Getter
@RequiredArgsConstructor
enum class PathType {
    SUBWAY("지하철"),
    WAY("길찾기"),
    PATROL("순찰");

    private val name: String? = null

    companion object {
        fun from(type: String?): PathType? {
            return Arrays.stream<PathType?>(entries.toTypedArray())
                .filter { e: PathType? -> e!!.name.equals(type, ignoreCase = true) }
                .findFirst()
                .orElseThrow<CustomException?>(Supplier { CustomException(ErrorCode.NOT_FOUND_PATH_TYPE, type) })
        }
    }
}
