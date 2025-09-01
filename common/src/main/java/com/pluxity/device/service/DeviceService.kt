package com.pluxity.device.service

import com.pluxity.device.dto.DeviceCreateRequest
import com.pluxity.device.dto.DeviceResponse
import com.pluxity.device.dto.DeviceUpdateRequest
import com.pluxity.device.dto.TypeKeyLabelResponse
import com.pluxity.device.dto.toDeviceResponse
import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCategory
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType
import com.pluxity.device.repository.DeviceRepository
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.service.FileService
import com.pluxity.global.annotation.CheckPermission
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.MappingUtils
import com.pluxity.user.entity.ExecutionPhase
import com.pluxity.user.entity.PermissionType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Stream

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
        return deviceRepository
            .save(
                Device(
                    id = request.id,
                    name = request.name,
                    category = category,
                    deviceType = request.deviceType,
                    companyType = request.companyType,
                ),
            ).id
    }

    @Transactional(readOnly = true)
    @CheckPermission(type = PermissionType.ID)
    fun findById(id: String): DeviceResponse = getDevice(id).toDeviceResponse(getThumbnailFile(getDevice(id)))

    private fun getThumbnailFile(device: Device): FileResponse? =
        device.category?.let {
            fileService.getFileResponse(it.iconFileId)
        }

    private fun getDevice(id: String): Device =
        deviceRepository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_DEVICE, id)

    @Transactional(readOnly = true)
    @CheckPermission(type = PermissionType.ID, phase = ExecutionPhase.FILTER)
    fun findAll(): List<DeviceResponse> {
        val devices = deviceRepository.findAll()
        val categoryList =
            devices
                .mapNotNull { it.category }
        val fileMap =
            MappingUtils.getFileMapByIds(
                categoryList,
                { v: DeviceCategory -> Stream.of(v.iconFileId) },
                fileService,
            )
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
