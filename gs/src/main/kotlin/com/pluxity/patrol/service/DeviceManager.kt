package com.pluxity.patrol.service

import com.pluxity.cctv.repository.CctvRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.constant.DeviceType
import com.pluxity.temperaturehumidity.repository.TemperatureHumidityRepository
import org.springframework.stereotype.Component

@Component
class DeviceManager(
    private val cctvRepository: CctvRepository,
    private val temperatureHumidityRepository: TemperatureHumidityRepository,
) {
    fun validateDevicesExist(deviceIdsByType: Map<DeviceType, List<String>>) {
        deviceIdsByType.forEach { (deviceType, deviceIds) ->
            val foundIds =
                when (deviceType) {
                    DeviceType.CCTV -> cctvRepository.findExistingIds(deviceIds)
                    DeviceType.TEMPERATURE_HUMIDITY -> temperatureHumidityRepository.findExistingIds(deviceIds)
                    else -> return
                }

            val missingIds = deviceIds - foundIds.toSet()
            if (missingIds.isNotEmpty()) {
                throw CustomException(ErrorCode.NOT_FOUND_DEVICES_BY_TYPE, deviceType, missingIds.joinToString(","))
            }
        }
    }
}
