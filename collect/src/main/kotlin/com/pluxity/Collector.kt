package com.pluxity

import com.pluxity.climate.ClimateDataCollector
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType
import com.pluxity.device.repository.DeviceRepository
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
    private val deviceRepository: DeviceRepository,
) {
    @Scheduled(cron = "0 0/1 * * * *")
    fun collectData() {
        runBlocking {
            supervisorScope {
                DeviceCompanyType.entries
                    .map { deviceCompanyType ->
                        launch {
                            runCatching {
                                when (deviceCompanyType) {
                                    DeviceCompanyType.DAWONDNS -> {
                                        val devices =
                                            deviceRepository.findByCompanyTypeAndDeviceType(
                                                DeviceCompanyType.DAWONDNS,
                                                DeviceType.TEMP_HUM,
                                            )
                                        climateDataCollector.collectClimateData(
                                            devices
                                                .filter { it.id.startsWith(DeviceCompanyType.DAWONDNS.toString()) }
                                                .map { it.id },
                                        )
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
}
