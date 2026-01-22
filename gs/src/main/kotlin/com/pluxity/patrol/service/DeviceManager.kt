package com.pluxity.patrol.service

import com.pluxity.cctv.repository.CctvRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.constant.DeviceAction
import com.pluxity.patrol.constant.DeviceType
import com.pluxity.temperaturehumidity.repository.TemperatureHumidityRepository
import org.springframework.stereotype.Component

@Component
class DeviceManager(
    private val cctvRepository: CctvRepository,
    private val temperatureHumidityRepository: TemperatureHumidityRepository,
) {
    private val deviceExistsCheckers: Map<DeviceType, (String) -> Boolean> =
        mapOf(
            DeviceType.CCTV to { id -> cctvRepository.existsById(id) },
            DeviceType.TEMPERATURE_HUMIDITY to { id -> temperatureHumidityRepository.existsById(id) },
        )

    fun checkDeviceExists(
        deviceType: DeviceType,
        deviceId: String,
    ) {
        val checker =
            deviceExistsCheckers[deviceType]
                ?: throw CustomException(ErrorCode.INVALID_DEVICE_TYPE)

        if (!checker(deviceId)) {
            throw CustomException(ErrorCode.NOT_FOUND_DEVICE, deviceId)
        }
    }

    fun validateAction(
        type: DeviceType,
        action: DeviceAction,
    ) {
        if (!type.execute(action)) {
            throw CustomException(ErrorCode.INVALID_DEVICE_ACTION, type, action)
        }
    }
}
