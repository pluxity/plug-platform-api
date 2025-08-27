package com.pluxity.device.entity

import com.pluxity.device.dto.TypeKeyValueResponse

enum class DeviceType(
    private val description: String,
) {
    TEMP_HUM("온습도계"),
    ;

    companion object {
        fun toKeyValueList(): List<TypeKeyValueResponse> = entries.map { TypeKeyValueResponse(it.name, it.description) }
    }
}
