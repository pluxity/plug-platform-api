package com.pluxity.device.entity

import com.pluxity.device.dto.TypeKeyLabelResponse

enum class DeviceCompanyType(
    private val description: String,
) {
    DAWONDNS("다원DNS"),
    ;

    companion object {
        fun toKeyValueList(): List<TypeKeyLabelResponse> = DeviceCompanyType.entries.map { TypeKeyLabelResponse(it.name, it.description) }
    }
}
