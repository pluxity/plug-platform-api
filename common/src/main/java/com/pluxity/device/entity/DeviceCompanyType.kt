package com.pluxity.device.entity

import com.pluxity.device.dto.TypeKeyValueResponse

enum class DeviceCompanyType(
    private val description: String,
) {
    DAWONDNS("다원DNS"),
    ;

    companion object {
        fun toKeyValueList(): List<TypeKeyValueResponse> = DeviceCompanyType.entries.map { TypeKeyValueResponse(it.name, it.description) }
    }
}
