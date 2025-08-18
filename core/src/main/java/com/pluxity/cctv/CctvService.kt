package com.pluxity.cctv

import com.pluxity.cctv.dto.CctvCreateRequest
import com.pluxity.cctv.dto.CctvResponse
import com.pluxity.cctv.dto.CctvUpdateRequest
import com.pluxity.cctv.dto.toCctvResponse
import com.pluxity.cctv.entity.Cctv
import com.pluxity.cctv.repository.CctvRepository
import com.pluxity.cctv.repository.DeviceCctvRepository
import com.pluxity.device.entity.DeviceCategory
import com.pluxity.device.service.DeviceCategoryService
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.MappingUtils
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Stream

@Service
class CctvService(
    private val cctvRepository: CctvRepository,
    private val deviceCategoryService: DeviceCategoryService,
    private val fileService: FileService,
    private val deviceCctvRepository: DeviceCctvRepository,
) {
    @Transactional
    fun create(request: CctvCreateRequest): String {
        val category =
            MappingUtils.findByIdIfExists<Long, DeviceCategory>(
                request.categoryId,
                deviceCategoryService::findById,
            )

        return cctvRepository
            .save(Cctv(id = request.id, cctvName = request.name, url = request.url, category = category))
            .id
    }

    @Transactional(readOnly = true)
    fun findAll(): List<CctvResponse> {
        val list = cctvRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
        val categoryList = list.mapNotNull { it.category }
        val fileMap =
            MappingUtils.getFileMapByIds(
                categoryList,
                { v: DeviceCategory -> Stream.of(v.iconFileId) },
                fileService,
            )
        return list.map { it.toCctvResponse(fileMap[it.category?.iconFileId]) }
    }

    @Transactional(readOnly = true)
    fun getById(id: String): CctvResponse {
        val cctv = findById(id)
        return cctv.toCctvResponse(getThumbnailFile(cctv))
    }

    @Transactional
    fun update(
        id: String,
        request: CctvUpdateRequest,
    ) {
        val cctv = findById(id)
        cctv.updateCctv(request)
        val category =
            MappingUtils.findByIdIfExists<Long, DeviceCategory>(
                request.categoryId,
                deviceCategoryService::findById,
            )
        cctv.changeCategory(category)
    }

    @Transactional
    fun delete(id: String) {
        val cctv = findById(id)
        deviceCctvRepository.deleteByCctvIdIn(listOf(cctv.id))
        cctvRepository.deleteById(cctv.id)
    }

    @Transactional(readOnly = true)
    fun findById(id: String): Cctv =
        cctvRepository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_CCTV, id)

    private fun getThumbnailFile(cctv: Cctv): FileResponse? = cctv.category?.iconFileId?.let(fileService::getFileResponse)
}
