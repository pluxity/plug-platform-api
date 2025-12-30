package com.pluxity.temperaturehumidity.repository

import com.pluxity.temperaturehumidity.entity.TemperatureHumidity

interface TemperatureHumidityCustomRepository {
    fun findByIdOrNullCustom(id: String): TemperatureHumidity?

    fun findAllByFacilityIdIfPresent(facilityId: Long?): List<TemperatureHumidity>
}
