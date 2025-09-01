package com.pluxity.device.service

import com.pluxity.category.service.CategoryService
import com.pluxity.device.dto.DeviceCategoryDepthResponse
import com.pluxity.device.dto.DeviceCategoryRequest
import com.pluxity.device.dto.DeviceCategoryResponse
import com.pluxity.device.dto.DeviceCategoryUpdateRequest
import com.pluxity.device.dto.DeviceResponse
import com.pluxity.device.dto.toDeviceCategoryResponse
import com.pluxity.device.dto.toDeviceResponse
import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCategory
import com.pluxity.device.repository.DeviceCategoryRepository
import com.pluxity.device.repository.DeviceRepository
import com.pluxity.facility.FacilityService
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.MappingUtils
import com.pluxity.global.utils.SortUtils
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Stream

@Service
class DeviceCategoryService(
    private val deviceCategoryRepository: DeviceCategoryRepository,
    private val deviceRepository: DeviceRepository,
    private val fileService: FileService,
    private val facilityService: FacilityService,
    override val repository: JpaRepository<DeviceCategory, Long>,
) : CategoryService<DeviceCategory>() {
    @Transactional
    fun create(request: DeviceCategoryRequest): Long {
        val deviceCategory =
            DeviceCategory(
                name = request.name,
                iconFileId = request.thumbnailFileId,
            )
        val parent =
            MappingUtils.findByIdIfExists(
                request.parentId,
            ) { id -> id?.let { super.findById(it) } }

        val deviceCategoryId = super.create(deviceCategory, parent)

        request.thumbnailFileId?.let { thumbnailId ->
            deviceCategory.updateIconFileId(thumbnailId)
            fileService.finalizeUpload(
                thumbnailId,
                "$DEVICE_CATEGORIES$deviceCategoryId/",
            )
        }

        return deviceCategoryId
    }

    @Transactional(readOnly = true)
    fun getDeviceCategory(id: Long): DeviceCategoryResponse {
        val allCategories: List<DeviceCategoryResponse> = getDeviceCategories()
        return findCategoryInTree(allCategories, id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_DEVICE_CATEGORY, id)
    }

    private fun findCategoryInTree(
        categories: List<DeviceCategoryResponse>,
        id: Long,
    ): DeviceCategoryResponse? =
        categories.firstOrNull { it.id == id }
            ?: categories
                .asSequence()
                .filter { it.children.isNotEmpty() }
                .mapNotNull { findCategoryInTree(it.children, id) }
                .firstOrNull()

    @Transactional(readOnly = true)
    fun getDeviceCategories(): List<DeviceCategoryResponse> {
        val allCategories: List<DeviceCategory> =
            deviceCategoryRepository.findAllBy(SortUtils.getOrderByCreatedAtDesc())

        val fileMap =
            MappingUtils.getFileMapByIds(
                allCategories,
                { deviceCategory: DeviceCategory ->
                    Stream.of(
                        deviceCategory.iconFileId,
                    )
                },
                fileService,
            )

        val list: List<DeviceCategoryResponse> =
            allCategories.map { it.toDeviceCategoryResponse(fileMap[it.iconFileId] ?: FileResponse.empty()) }

        return MappingUtils.makeCategoryTree(
            list,
            DeviceCategoryResponse::id,
            DeviceCategoryResponse::parentId,
            DeviceCategoryResponse::children,
        )
    }

    @Transactional(readOnly = true)
    fun getChildDeviceCategories(parentId: Long): List<DeviceCategoryResponse> =
        deviceCategoryRepository.findByParentId(parentId).map { category ->
            createDeviceCategoryResponseWithoutChildren(category)
        }

    private fun createDeviceCategoryResponseWithoutChildren(category: DeviceCategory): DeviceCategoryResponse {
        val iconFile = category.iconFileId?.let { fileService.getFileResponse(it) } ?: FileResponse.empty()
        return category.toDeviceCategoryResponse(iconFile)
    }

    @Transactional
    fun update(
        id: Long,
        request: DeviceCategoryUpdateRequest,
    ) {
        val deviceCategory = findById(id)
        deviceCategory.updateName(request.name)

        // 부모 카테고리 업데이트
        request.parentId?.let { parentId ->
            super.update(id, request.name, parentId)
        } ?: deviceCategory.assignToRootPreservingEntity()

        // 썸네일 파일 업데이트 (중복 제거)
        request.thumbnailFileId?.let { thumbnailId ->
            deviceCategory.updateIconFileId(thumbnailId)
            fileService.finalizeUpload(
                thumbnailId,
                "$DEVICE_CATEGORIES${deviceCategory.id}/",
            )
        } ?: deviceCategory.updateIconFileId(null)
    }

    @Transactional
    fun delete(id: Long) {
        val deviceCategory = findById(id)

        if (deviceCategory.children.isNotEmpty()) {
            throw CustomException(ErrorCode.CATEGORY_HAS_CHILDREN)
        }

        if (deviceCategory.devices.isNotEmpty()) {
            throw CustomException(ErrorCode.CATEGORY_HAS_DEVICES)
        }

        deviceCategoryRepository.delete(deviceCategory)
    }

    @Transactional(readOnly = true)
    fun getDevicesByCategoryId(
        id: Long,
        facilityId: Long,
    ): List<DeviceResponse> {
        val category = findById(id)
        val facility = facilityService.findById(facilityId)
        val list: List<Device> = deviceRepository.findByCategoryAndFacility(category, facility)
        val fileMap =
            MappingUtils.getFileMapByIds(
                list,
                { v: Device -> Stream.of(v.category?.iconFileId) },
                fileService,
            )
        return list.map { it.toDeviceResponse(fileMap[it.category?.iconFileId]) }
    }

    fun getDeviceCategoryDepth(): DeviceCategoryDepthResponse = DeviceCategoryDepthResponse(DeviceCategory("").maxDepth)

    companion object {
        const val DEVICE_CATEGORIES: String = "device-categories/"
    }
}
