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
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.extensions.getFileMapById
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
class DeviceService(
    private val deviceRepository: DeviceRepository,
    private val fileService: FileService,
    private val deviceCategoryService: DeviceCategoryService,
) {
    @Transactional
    fun save(request: DeviceCreateRequest): String {
        val category = request.categoryId?.let { deviceCategoryService.findById(request.categoryId) }

        val device = Device(
            id = request.id,
            name = request.name,
            category = null,
            deviceType = request.deviceType,
            companyType = request.companyType,
        )
        val savedDevice = deviceRepository.save(device)
        savedDevice.changeCategory(category)

        return savedDevice.id
    }

    @Transactional(readOnly = true)
    fun findById(id: String): DeviceResponse = getDevice(id).toDeviceResponse(getThumbnailFile(getDevice(id)))

    private fun getThumbnailFile(device: Device): FileResponse? =
        device.category?.let {
            fileService.getFileResponse(it.iconFileId)
        }

    private fun getDevice(id: String): Device =
        deviceRepository.findByIdOrNullCustom(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_DEVICE, id)

    @Transactional(readOnly = true)
    fun findAll(facilityId: Long? = null): List<DeviceResponse> {
        val devices = deviceRepository.findAllByFacilityIdIfPresent(facilityId)

        val categoryList =
            devices
                .mapNotNull { it.category }
        val fileMap = fileService.getFileMapById(categoryList) { it.iconFileId }
        return devices.map {
            it.toDeviceResponse(fileMap[it.category?.iconFileId])
        }
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
        val category = request.categoryId?.let { deviceCategoryService.findById(it) }
        device.changeCategory(category)
    }

    @Transactional
    fun delete(id: String) {
        val device = getDevice(id)
        device.clearAllRelations()
        deviceRepository.deleteById(device.id)
    }

    @Transactional
    fun assignCategory(
        deviceId: String,
        categoryId: Long,
    ) {
        val device = getDevice(deviceId)
        device.changeCategory(deviceCategoryService.findById(categoryId))
        log.info { "디바이스 [$deviceId]에 카테고리 [$categoryId]가 할당되었습니다." }
    }

    @Transactional
    fun removeCategory(deviceId: String) {
        val device = getDevice(deviceId)
        device.category ?: throw CustomException(ErrorCode.NOT_FOUND_ASSIGN_DEVICE_CATEGORY, deviceId)
        device.changeCategory(null)
        log.info { "디바이스 [$deviceId]에서 카테고리가 제거되었습니다." }
    }
}
