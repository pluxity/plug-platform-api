package com.pluxity.device.entity

import com.pluxity.device.dto.TypeKeyLabelResponse

enum class DeviceType(
    private val description: String,
) {
    TEMP_HUM("온습도계"),
    ;

    companion object {
        fun toKeyValueList(): List<TypeKeyLabelResponse> = entries.map { TypeKeyLabelResponse(it.name, it.description) }
    }
}
