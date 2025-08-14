package com.pluxity.device

import com.pluxity.cctv.CctvService
import com.pluxity.cctv.dto.CctvResponse
import com.pluxity.cctv.entity.Cctv
import com.pluxity.cctv.entity.DeviceCctv
import com.pluxity.cctv.repository.DeviceCctvRepository
import com.pluxity.device.dto.DeviceCategoryResponseWithoutChildren
import com.pluxity.device.dto.GsDeviceCctvUpdateRequest
import com.pluxity.device.dto.GsDeviceCreateRequest
import com.pluxity.device.dto.GsDeviceResponse
import com.pluxity.device.dto.GsDeviceUpdateRequest
import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCategory
import com.pluxity.device.service.DeviceCategoryService
import com.pluxity.feature.dto.FeatureResponse
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
class GsDeviceService(
    private val repository: GsDeviceRepository,
    private val deviceCategoryService: DeviceCategoryService,
    private val deviceCctvRepository: DeviceCctvRepository,
    private val cctvService: CctvService,
    private val fileService: FileService,
) {
    @Transactional
    fun save(request: GsDeviceCreateRequest): String {
        val category = request.categoryId?.let { deviceCategoryService.findById(request.categoryId) }
        return repository.save(GsDevice(request.id, category, request.name)).id
    }

    @Transactional(readOnly = true)
    @CheckPermissionCategory(categoryResourceType = ResourceType.DEVICE_CATEGORY)
    fun findById(id: String): GsDeviceResponse = createResponse(getDevice(id), getThumbnailFile(getDevice(id)))

    private fun getThumbnailFile(gsDevice: Device): FileResponse? =
        gsDevice.category?.let {
            fileService.getFileResponse(it.iconFileId)
        }

    private fun getDevice(id: String): GsDevice =
        repository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_DEVICE, id)

    @Transactional(readOnly = true)
    @CheckPermissionCategory(categoryResourceType = ResourceType.DEVICE_CATEGORY)
    fun findAll(): List<GsDeviceResponse> {
        val gsDevices = repository.findAll()
        val categoryList =
            gsDevices
                .mapNotNull { it.category }
        val fileMap =
            MappingUtils.getFileMapByIds(
                categoryList,
                { v: DeviceCategory -> Stream.of(v.iconFileId) },
                fileService,
            )
        return gsDevices.map { gsDevice: GsDevice ->
            createResponse(
                gsDevice,
                fileMap[gsDevice.category?.iconFileId],
            )
        }
    }

    @Transactional
    fun update(
        id: String,
        request: GsDeviceUpdateRequest,
    ) {
        val device = getDevice(id)
        device.update(request.name)
        request.categoryId?.let { categoryId ->
            device.changeCategory(deviceCategoryService.findById(categoryId))
        }
    }

    @Transactional
    fun putUpdate(
        id: String,
        request: GsDeviceUpdateRequest,
    ) {
        val device = getDevice(id)
        device.putUpdate(request.name)
        val category = request.categoryId?.let(deviceCategoryService::findById)
        device.changeCategory(category)
    }

    @Transactional
    fun delete(id: String) {
        val device = getDevice(id)
        device.clearAllRelations()
        deviceCctvRepository.deleteByDevice(device)
        repository.delete(device)
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

    @Transactional(readOnly = true)
    fun getCctvByDeviceId(deviceId: String): List<CctvResponse> {
        val device = getDevice(deviceId)
        val deviceCctvs = deviceCctvRepository.findByDevice(device)
        return deviceCctvs
            .map { it.cctv }
            .map { cctv: Cctv ->
                CctvResponse(
                    cctv.id,
                    cctv.name,
                    cctv.url,
                    cctv.feature?.let { FeatureResponse.from(it) },
                    cctv.category?.let {
                        DeviceCategoryResponseWithoutChildren.from(
                            it,
                            getThumbnailFile(cctv),
                        )
                    },
                )
            }
    }

    @Transactional
    fun assignCctvToDevice(
        deviceId: String,
        request: GsDeviceCctvUpdateRequest,
    ) {
        val device = getDevice(deviceId)
        val existIds = deviceCctvRepository.findByDevice(device).map { it.cctv.id!! }

        // 추가할 cctv id
        val saveList =
            request.cctvIds
                .filter { id: String -> !existIds.contains(id) }
                .map { cctvId: String ->
                    DeviceCctv.builder().cctv(cctvService.findById(cctvId)).device(device).build()
                }

        // 삭제할 cctv id
        val removeList = existIds.filter { id: String -> !request.cctvIds.contains(id) }

        // 추가
        if (saveList.isNotEmpty()) {
            deviceCctvRepository.saveAll(saveList)
        }

        // 삭제
        removeList.takeIf { it.isNotEmpty() }?.let(deviceCctvRepository::deleteByCctvIdIn)
    }

    private fun createResponse(
        gsDevice: GsDevice,
        thumbnailFile: FileResponse?,
    ): GsDeviceResponse {
        return GsDeviceResponse(
            gsDevice.id,
            gsDevice.name,
            gsDevice.feature?.let { FeatureResponse.from(it) },
            gsDevice.category?.let { DeviceCategoryResponseWithoutChildren.from(it, thumbnailFile) },
        )
    }
}
