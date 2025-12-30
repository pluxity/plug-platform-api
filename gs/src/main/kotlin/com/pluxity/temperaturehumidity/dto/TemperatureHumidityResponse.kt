package com.pluxity.temperaturehumidity.dto

import com.pluxity.feature.dto.FeatureResponse
import com.pluxity.feature.dto.toFeatureResponse
import com.pluxity.temperaturehumidity.entity.TemperatureHumidity

class TemperatureHumidityResponse(
    val id: String,
    val name: String,
    val feature: FeatureResponse?,
)

fun TemperatureHumidity.toTemperatureHumidityResponse() =
    TemperatureHumidityResponse(
        id = this.id,
        name = this.name,
        feature = this.feature?.toFeatureResponse(),
    )
