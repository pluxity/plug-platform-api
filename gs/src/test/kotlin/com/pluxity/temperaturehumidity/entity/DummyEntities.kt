package com.pluxity.temperaturehumidity.entity

import com.pluxity.feature.entity.Feature
import com.pluxity.temperaturehumidity.entity.TemperatureHumidity

fun dummyTemperatureHumidity(
    id: String = "thId",
    name: String = "thName",
    feature: Feature? = null,
): TemperatureHumidity = TemperatureHumidity(id, name, feature)
