package com.pluxity.facility

import com.pluxity.facility.dto.FacilityCreateRequest
import com.pluxity.facility.dto.FacilityDrawingUpdateRequest
import com.pluxity.facility.dto.FacilityFloorUpdateRequest
import com.pluxity.facility.dto.FacilityHistoryResponse
import com.pluxity.facility.dto.FacilityLocationUpdateRequest
import com.pluxity.facility.dto.FacilityPathSaveRequest
import com.pluxity.facility.dto.FacilityPathUpdateRequest
import com.pluxity.facility.dto.FacilityResponse
import com.pluxity.facility.dto.FacilityUpdateRequest
import com.pluxity.facility.dto.toResponse
import com.pluxity.facility.history.FacilityHistoryService
import com.pluxity.facility.path.FacilityPathService
import com.pluxity.facility.strategy.FloorService
import com.pluxity.file.extensions.getFileMapByIds
import com.pluxity.file.service.FileService
import com.pluxity.global.annotation.CheckPermission
import com.pluxity.global.constant.ErrorCode.DUPLICATE_FACILITY_CODE
import com.pluxity.global.constant.ErrorCode.NOT_FOUND_FACILITY
import com.pluxity.global.constant.ErrorCode.NOT_FOUND_FACILITY_CODE
import com.pluxity.global.exception.CustomException
import com.pluxity.user.entity.PermissionCheckType
import com.pluxity.user.entity.PermissionType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.get

@Service
class FacilityService(
    private val facilityRepository: FacilityRepository,
    private val fileService: FileService,
    private val facilityHistoryService: FacilityHistoryService,
    private val facilityPathService: FacilityPathService,
    private val floorService: FloorService,
) {
    private val prefix = "facilities/"

    @Transactional
    fun save(
        facility: Facility,
        request: FacilityCreateRequest,
    ): Facility {
        request.code.let { code ->
            if (code.isNotEmpty()) {
                validateCodeUniqueness(code)
                facility.updateCode(code)
            }
        }

        val savedFacility = facilityRepository.save(facility)
        val filePath = "$prefix${savedFacility.id}/"

        request.drawingFileId?.let { drawingFileId ->
            val drawingFile = fileService.finalizeUpload(drawingFileId, filePath)
            facility.updateDrawingFile(drawingFile)
            facilityHistoryService.save(drawingFileId, facility.id!!, "최초등록")
        }

        request.thumbnailFileId?.let { thumbnailFileId ->
            val thumbnailFile = fileService.finalizeUpload(thumbnailFileId, filePath)
            facility.updateThumbnailFile(thumbnailFile)
        }

        facility.updatePosition(request.lon, request.lat, request.locationMeta)

        return savedFacility
    }

    private fun validateCodeUniqueness(code: String) {
        if (facilityRepository.existsByCode(code)) {
            throw CustomException(DUPLICATE_FACILITY_CODE, code)
        }
    }

    @CheckPermission(type = PermissionType.ID)
    @Transactional(readOnly = true)
    fun findByCode(code: String): Facility =
        facilityRepository
            .findByCode(code)
            ?: throw CustomException(NOT_FOUND_FACILITY_CODE, code)

    @CheckPermission(type = PermissionType.ID)
    @Transactional(readOnly = true)
    fun findById(id: Long): Facility =
        facilityRepository
            .findByIdOrNull(id)
            ?: throw CustomException(NOT_FOUND_FACILITY, id)

    @CheckPermission(type = PermissionType.ID, phase = PermissionCheckType.ITEM_LIST)
    @Transactional(readOnly = true)
    fun findAll(): List<Facility> = facilityRepository.findAll()

    @Transactional
    fun update(
        id: Long,
        request: FacilityUpdateRequest,
    ) {
        val facility = findById(id)

        request.code?.let { newCode ->
            if (newCode != facility.code) {
                validateCodeUniqueness(newCode)
                facility.updateCode(newCode)
            }
        }

        request.name?.let { facility.updateName(it) }
        request.description?.let { facility.updateDescription(it) }

        request.thumbnailFileId?.let { thumbnailFileId ->
            if (thumbnailFileId != facility.thumbnailFileId) {
                val filePath = "$prefix${facility.id}/"
                val thumbnailFile = fileService.finalizeUpload(thumbnailFileId, filePath)
                facility.updateThumbnailFile(thumbnailFile)
            }
        }

        facility.updatePosition(request.lon, request.lat, request.locationMeta)
    }

    @Transactional
    fun putUpdate(
        id: Long,
        request: FacilityUpdateRequest,
    ) {
        val facility = findById(id)

        request.code?.let { newCode ->
            if (newCode != facility.code) {
                validateCodeUniqueness(newCode)
            }
        }

        facility.updateCode(request.code)
        facility.updateName(request.name!!)
        facility.updateDescription(request.description)

        if (request.thumbnailFileId != facility.thumbnailFileId) {
            request.thumbnailFileId?.let { thumbnailFileId ->
                val thumbnailFile = fileService.finalizeUpload(thumbnailFileId, "${prefix}${facility.id}/")
                facility.updateThumbnailFile(thumbnailFile)
            } ?: facility.updateThumbnailFileId(null)
        }

        facility.updatePosition(request.lon, request.lat, request.locationMeta)
    }

    @Transactional
    fun update(
        id: Long,
        newFacility: Facility,
    ) {
        val facility = findById(id)

        newFacility.code?.let { newCode ->
            if (newCode != facility.code) {
                validateCodeUniqueness(newCode)
            }
        }

        facility.update(newFacility)
        facilityRepository.save(facility)
    }

    @Transactional
    fun deleteFacility(id: Long) {
        val facility = findById(id)
        facilityRepository.delete(facility)
    }

    @Transactional(readOnly = true)
    fun findFacilityHistories(facilityId: Long): List<FacilityHistoryResponse> {
        facilityRepository
            .findByIdOrNull(facilityId)
            ?: throw CustomException(NOT_FOUND_FACILITY, facilityId)
        return facilityHistoryService.findByFacilityId(facilityId)
    }

    @Transactional
    fun updateDrawingFile(
        id: Long,
        request: FacilityDrawingUpdateRequest,
    ) {
        val facility = findById(id)
        val filePath = "$prefix${facility.id}/"
        val drawingFile = fileService.finalizeUpload(request.drawingFileId, filePath)
        facility.updateDrawingFile(drawingFile)
        facilityHistoryService.save(request.drawingFileId, facility.id!!, request.comment ?: "")
    }

    @Transactional
    fun savePath(
        facilityId: Long,
        request: FacilityPathSaveRequest,
    ) {
        val facility = findById(facilityId)
        facilityPathService.save(facility, request.name, request.type, request.path)
    }

    @Transactional
    fun updatePath(
        facilityId: Long,
        pathId: Long,
        request: FacilityPathUpdateRequest,
    ) {
        findById(facilityId)
        facilityPathService.update(pathId, request.name, request.type, request.path)
    }

    @Transactional
    fun deletePath(
        facilityId: Long,
        pathId: Long,
    ) {
        findById(facilityId)
        facilityPathService.delete(pathId)
    }

    @Transactional
    fun updateLocation(
        facilityId: Long,
        request: FacilityLocationUpdateRequest,
    ) {
        val facility = findById(facilityId)
        facility.updatePosition(request.lon, request.lat, request.locationMeta)
    }

    @Transactional
    fun updateFloor(
        facilityId: Long,
        request: FacilityFloorUpdateRequest,
    ) {
        val facility = findById(facilityId)
        floorService.update(facility, request.floors)
    }

    @Transactional(readOnly = true)
    fun findAllFacilities(): List<FacilityResponse> {
        val list = facilityRepository.findAllByOrderByCreatedAtDesc()
        val fileMap =
            fileService.getFileMapByIds(list) {
                listOfNotNull(it.drawingFileId, it.thumbnailFileId)
            }

        return list.map { entity ->
            entity.toResponse(
                fileMap[entity.drawingFileId],
                fileMap[entity.thumbnailFileId],
            )
        }
    }
}
