package com.pluxity

import com.pluxity.climate.ClimateDataCollector
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.repository.DeviceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("!local")
class Collector(
    private val climateDataCollector: ClimateDataCollector,
    private val deviceRepository: DeviceRepository,
) {
    @Scheduled(cron = "0 0/1 * * * *")
    fun collectData() {
        runBlocking {
            coroutineScope {
                val devices = deviceRepository.findAll()
                DeviceCompanyType.entries
                    .map { deviceCompanyType ->
                        async {
                            when (deviceCompanyType) {
                                DeviceCompanyType.DAWONDNS -> {
                                    climateDataCollector.collectClimateData(
                                        devices
                                            .filter { it.companyType == DeviceCompanyType.DAWONDNS }
                                            .map { it.id },
                                    )
                                }
                            }
                        }
                    }.awaitAll()
            }
        }
    }
}
