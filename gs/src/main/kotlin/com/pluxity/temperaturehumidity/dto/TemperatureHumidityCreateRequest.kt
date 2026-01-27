package com.pluxity.temperaturehumidity.dto

import io.swagger.v3.oas.annotations.media.Schema

class TemperatureHumidityCreateRequest(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "온습도계 아이디")
    val id: String,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "온습도계 명")
    val name: String,
)
