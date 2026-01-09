package com.pluxity.temperaturehumidity

import com.pluxity.permission.ResourceDataProvider
import com.pluxity.permission.ResourceType
import com.pluxity.permission.dto.ResourceItemResponse
import com.pluxity.temperaturehumidity.repository.TemperatureHumidityRepository
import org.springframework.stereotype.Component

@Component
class TemperatureHumidityResourceDataProvider(
    private val temperatureHumidityRepository: TemperatureHumidityRepository,
) : ResourceDataProvider {
    override val resourceType: ResourceType = ResourceType.TEMPERATURE_HUMIDITY

    override fun findAllResources(): List<ResourceItemResponse> =
        temperatureHumidityRepository.findAll().map {
            ResourceItemResponse(
                id = it.id,
                name = it.name,
            )
        }
}
