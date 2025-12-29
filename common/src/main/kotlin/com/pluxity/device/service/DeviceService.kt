package com.pluxity.device.service

import com.pluxity.device.dto.DeviceCreateRequest
import com.pluxity.device.dto.DeviceResponse
import com.pluxity.device.dto.DeviceUpdateRequest
import com.pluxity.device.dto.TypeKeyLabelResponse
import com.pluxity.device.dto.toDeviceResponse
import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType
import com.pluxity.device.repository.DeviceRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeviceService(
    private val deviceRepository: DeviceRepository,
) {
    @Transactional
    fun save(request: DeviceCreateRequest): String =
        deviceRepository
            .save(
                Device(
                    id = request.id,
                    name = request.name,
                    deviceType = request.deviceType,
                    companyType = request.companyType,
                ),
            ).id

    @Transactional(readOnly = true)
    fun findById(id: String): DeviceResponse = getDevice(id).toDeviceResponse()

    private fun getDevice(id: String): Device =
        deviceRepository.findByIdOrNullCustom(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_DEVICE, id)

    @Transactional(readOnly = true)
    fun findAll(facilityId: Long? = null): List<DeviceResponse> {
        val devices = deviceRepository.findAllByFacilityIdIfPresent(facilityId)
        return devices.map { it.toDeviceResponse() }
    }

    fun findAllType(): List<TypeKeyLabelResponse> = DeviceType.toKeyValueList()

    fun findAllCompanyType(): List<TypeKeyLabelResponse> = DeviceCompanyType.toKeyValueList()

    @Transactional
    fun putUpdate(
        id: String,
        request: DeviceUpdateRequest,
    ) {
        val device = getDevice(id)
        device.putUpdate(request.name, request.deviceType, request.companyType)
    }

    @Transactional
    fun delete(id: String) {
        val device = getDevice(id)
        device.clearAllRelations()
        deviceRepository.deleteById(device.id)
    }
}
