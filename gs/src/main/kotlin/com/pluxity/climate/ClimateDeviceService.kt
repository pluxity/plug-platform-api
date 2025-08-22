package com.pluxity.climate

import com.pluxity.climate.dto.ClimateDeviceCreateRequest
import com.pluxity.climate.dto.ClimateDeviceUpdateRequest
import com.pluxity.climate.dto.ClimateResponse
import com.pluxity.climate.dto.toClimateResponse
import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCategory
import com.pluxity.device.service.DeviceCategoryService
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.service.FileService
import com.pluxity.global.annotation.CheckPermissionCategory
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.MappingUtils
import com.pluxity.permission.ResourceType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Stream

private val log = KotlinLogging.logger {}

@Service
class ClimateDeviceService(
    private val repository: ClimateDeviceRepository,
    private val deviceCategoryService: DeviceCategoryService,
    private val fileService: FileService,
) {
    @Transactional
    fun save(request: ClimateDeviceCreateRequest): String {
        val category = request.categoryId?.let { deviceCategoryService.findById(request.categoryId) }
        return repository.save(ClimateDevice(request.id, category, request.name)).id
    }

    @Transactional(readOnly = true)
    @CheckPermissionCategory(categoryResourceType = ResourceType.DEVICE_CATEGORY)
    fun findById(id: String): ClimateResponse = getClimateDevice(id).toClimateResponse(getThumbnailFile(getClimateDevice(id)))

    private fun getThumbnailFile(climate: Device): FileResponse? =
        climate.category?.let {
            fileService.getFileResponse(it.iconFileId)
        }

    private fun getClimateDevice(id: String): ClimateDevice =
        repository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_DEVICE, id)

    @Transactional(readOnly = true)
    @CheckPermissionCategory(categoryResourceType = ResourceType.DEVICE_CATEGORY)
    fun findAll(): List<ClimateResponse> {
        val climates = repository.findAll()
        val categoryList =
            climates
                .mapNotNull { it.category }
        val fileMap =
            MappingUtils.getFileMapByIds(
                categoryList,
                { v: DeviceCategory -> Stream.of(v.iconFileId) },
                fileService,
            )
        return climates.map {
            it.toClimateResponse(fileMap[it.category?.iconFileId])
        }
    }

    @Transactional
    fun putUpdate(
        id: String,
        request: ClimateDeviceUpdateRequest,
    ) {
        val device = getClimateDevice(id)
        device.putUpdate(request.name)
        val category = request.categoryId?.let { deviceCategoryService.findById(it) }
        device.changeCategory(category)
    }

    @Transactional
    fun delete(id: String) {
        val device = getClimateDevice(id)
        device.clearAllRelations()
        repository.deleteById(device.id)
    }

    @Transactional
    fun assignCategory(
        deviceId: String,
        categoryId: Long,
    ) {
        val device = getClimateDevice(deviceId)
        device.changeCategory(deviceCategoryService.findById(categoryId))
        log.info { "디바이스 [$deviceId]에 카테고리 [$categoryId]가 할당되었습니다." }
    }

    @Transactional
    fun removeCategory(deviceId: String) {
        val device = getClimateDevice(deviceId)
        device.category ?: throw CustomException(ErrorCode.NOT_FOUND_ASSIGN_DEVICE_CATEGORY, deviceId)
        device.changeCategory(null)
        log.info { "디바이스 [$deviceId]에서 카테고리가 제거되었습니다." }
    }
}
