package com.pluxity.facility

import com.pluxity.facility.dto.*
import com.pluxity.facility.history.FacilityHistoryService
import com.pluxity.facility.path.FacilityPathService
import com.pluxity.facility.strategy.FloorService
import com.pluxity.file.service.FileService
import com.pluxity.global.annotation.CheckPermission
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.MappingUtils
import com.pluxity.user.entity.ExecutionPhase
import com.pluxity.user.entity.PermissionType
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Supplier

@Service
@RequiredArgsConstructor
@Slf4j
class FacilityService {
    private val facilityRepository: FacilityRepository? = null
    private val fileService: FileService? = null

    private val PREFIX = "facilities/"
    private val facilityHistoryService: FacilityHistoryService? = null
    private val facilityPathService: FacilityPathService? = null
    private val floorService: FloorService? = null

    @Transactional
    fun save(facility: Facility, request: FacilityCreateRequest): Facility {
        // 코드 중복 검사
        if (request.code != null && !request.code.isEmpty()) {
            checkDuplicateCode(request.code)
            facility.updateCode(request.code)
        }

        val savedFacility = facilityRepository!!.save<Facility>(facility)

        val filePath = PREFIX + savedFacility.getId() + "/"
        if (request.drawingFileId != null) {
            facility.updateDrawingFileId(fileService!!.finalizeUpload(request.drawingFileId, filePath))
            facilityHistoryService!!.save(request.drawingFileId, facility.getId(), "최초등록")
        }

        if (request.thumbnailFileId != null) {
            facility.updateThumbnailFileId(
                fileService!!.finalizeUpload(request.thumbnailFileId, filePath)
            )
        }
        facility.updatePosition(
            FacilityPosition.builder()
                .lon(request.lon)
                .lat(request.lat)
                .locationMeta(request.locationMeta)
                .build()
        )

        return savedFacility
    }

    private fun checkDuplicateCode(code: String?) {
        if (facilityRepository!!.existsByCode(code)) {
            throw CustomException(ErrorCode.DUPLICATE_FACILITY_CODE, code)
        }
    }

    @CheckPermission(type = PermissionType.ID)
    @Transactional(readOnly = true)
    fun findByCode(code: String?): Facility? {
        return facilityRepository!!
            .findByCode(code)
            .orElseThrow<CustomException?>(Supplier { CustomException(ErrorCode.NOT_FOUND_FACILITY_CODE, code) })
    }

    @CheckPermission(type = PermissionType.ID)
    @Transactional(readOnly = true)
    fun findById(id: Long): Facility {
        return facilityRepository!!
            .findById(id)
            .orElseThrow<CustomException?>(Supplier { CustomException(ErrorCode.NOT_FOUND_FACILITY, id) })
    }

    @CheckPermission(type = PermissionType.ID, phase = ExecutionPhase.FILTER)
    @Transactional(readOnly = true)
    fun findAll(): MutableList<Facility?> {
        return facilityRepository!!.findAll()
    }

    @Transactional
    fun update(id: Long, request: FacilityUpdateRequest?) {
        if (request == null) {
            return
        }
        val facility = findById(id)

        // 코드 변경 요청이 있고, 기존 코드와 다른 경우에만 중복 검사
        if (request.code != null && request.code != facility.getCode()) {
            checkDuplicateCode(request.code)
            facility.updateCode(request.code)
        }

        if (request.name != null) {
            facility.updateName(request.name)
        }

        if (request.description != null) {
            facility.updateDescription(request.description)
        }

        if (request.thumbnailFileId != null
            && request.thumbnailFileId != facility.getThumbnailFileId()
        ) {
            val filePath = PREFIX + facility.getId() + "/"
            facility.updateThumbnailFileId(
                fileService!!.finalizeUpload(request.thumbnailFileId, filePath)
            )
        }
        facility.updatePosition(request.lon, request.lat, request.locationMeta)
    }

    @Transactional
    fun putUpdate(id: Long, request: FacilityUpdateRequest) {
        val facility = findById(id)

        if (request.code != null && request.code != facility.getCode()) {
            checkDuplicateCode(request.code)
        }

        facility.updateCode(request.code)
        facility.updateName(request.name)
        facility.updateDescription(request.description)

        if (request.thumbnailFileId != null
            && request.thumbnailFileId != facility.getThumbnailFileId()
        ) {
            val filePath = PREFIX + facility.getId() + "/"
            facility.updateThumbnailFileId(
                fileService!!.finalizeUpload(request.thumbnailFileId, filePath)
            )
        }
        facility.updateThumbnailFileId(request.thumbnailFileId)

        facility.updatePosition(request.lon, request.lat, request.locationMeta)
    }

    @Transactional
    fun update(id: Long, newFacility: Facility) {
        val facility = findById(id)

        // 코드 변경 요청이 있고, 기존 코드와 다른 경우에만 중복 검사
        if (newFacility.getCode() != null && newFacility.getCode() != facility.getCode()) {
            checkDuplicateCode(newFacility.getCode())
        }

        facility.update(newFacility)
        facilityRepository!!.save<Facility?>(facility)
    }

    @Transactional
    fun deleteFacility(id: Long) {
        val facility = findById(id)
        facilityRepository!!.delete(facility)
    }

    @Transactional(readOnly = true)
    fun findFacilityHistories(facilityId: Long): MutableList<FacilityHistoryResponse?>? {
        facilityRepository!!
            .findById(facilityId)
            .orElseThrow<CustomException?>(Supplier { CustomException(ErrorCode.NOT_FOUND_FACILITY, facilityId) })
        return facilityHistoryService!!.findByFacilityId(facilityId)
    }

    @Transactional
    fun updateDrawingFile(id: Long, request: FacilityDrawingUpdateRequest) {
        val facility = findById(id)
        val filePath = PREFIX + facility.getId() + "/"
        facility.updateDrawingFileId(fileService!!.finalizeUpload(request.drawingFileId, filePath))
        facilityHistoryService!!.save(request.drawingFileId, facility.getId(), request.comment)
    }

    @Transactional
    fun savePath(facilityId: Long, request: FacilityPathSaveRequest) {
        facilityPathService!!.save(findById(facilityId), request.name, request.type, request.path)
    }

    @Transactional
    fun updatePath(facilityId: Long, pathId: Long?, request: FacilityPathUpdateRequest) {
        findById(facilityId)
        facilityPathService!!.update(pathId, request.name, request.type, request.path)
    }

    @Transactional
    fun deletePath(facilityId: Long, pathId: Long?) {
        findById(facilityId)
        facilityPathService!!.delete(pathId)
    }

    @Transactional
    fun updateLocation(facilityId: Long, request: FacilityLocationUpdateRequest) {
        val facility = findById(facilityId)
        facility.updatePosition(
            FacilityPosition.builder()
                .lon(request.lon)
                .lat(request.lat)
                .locationMeta(request.locationMeta)
                .build()
        )
    }

    @Transactional
    fun updateFloor(facilityId: Long, request: FacilityFloorUpdateRequest) {
        val facility = findById(facilityId)
        floorService!!.update<Facility?>(facility, request.floors)
    }

    @Transactional(readOnly = true)
    fun findAllFacilities(): MutableList<FacilityResponse?>? {
        return MappingUtils.mapWithFiles(
            facilityRepository!!.findAllByOrderByCreatedAtDesc(), fileService
        )
    }
}
