package com.pluxity.collect

import com.pluxity.collect.climate.ClimateDataCollector
import com.pluxity.temperaturehumidity.repository.TemperatureHumidityRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("!local")
class Collector(
    private val climateDataCollector: ClimateDataCollector,
    private val temperatureHumidityRepository: TemperatureHumidityRepository,
) {
    @Scheduled(cron = "0 0/1 * * * *")
    fun collectData() {
        runBlocking {
            supervisorScope {
                launch {
                    runCatching {
                        val deviceIds =
                            temperatureHumidityRepository
                                .findAll()
                                .map { it.id }
                                .takeIf { it.isNotEmpty() }
                        if (deviceIds != null) {
                            climateDataCollector.collectClimateData(deviceIds)
                        }
                    }
                }
            }
        }
    }
}
