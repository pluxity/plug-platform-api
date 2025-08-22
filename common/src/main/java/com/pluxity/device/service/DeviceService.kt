package com.pluxity.device.service

import com.pluxity.device.dto.DeviceInfoResponse
import com.pluxity.device.repository.DeviceRepository
import org.springframework.stereotype.Service

@Service
class DeviceService(
    private val deviceRepository: DeviceRepository,
) {
    fun findAll(): List<DeviceInfoResponse> = deviceRepository.findAll().map { it.toDeviceInfo() }
}
