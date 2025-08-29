package com.pluxity.device.dto

import io.swagger.v3.oas.annotations.media.Schema

class TypeKeyValueResponse(
    @field:Schema(description = "코드")
    val key: String,
    @field:Schema(description = "표시 이름")
    val label: String,
)
