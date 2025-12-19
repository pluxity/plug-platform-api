package com.pluxity.building

import com.pluxity.building.dto.BuildingCreateRequest
import com.pluxity.building.dto.BuildingResponse
import com.pluxity.building.dto.BuildingUpdateRequest
import com.pluxity.facility.FacilityService
import com.pluxity.facility.dto.toResponse
import com.pluxity.facility.strategy.FloorService
import com.pluxity.file.extensions.getFileMapByIds
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.SortUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BuildingService(
    private val fileService: FileService,
    private val facilityService: FacilityService,
    private val floorService: FloorService,
    private val repository: BuildingRepository,
) {
    @Transactional
    fun save(request: BuildingCreateRequest): Long {
        val building = Building(request.facility.name, request.facility.description)

        val saved = facilityService.save(building, request.facility)

        floorService.save(saved, request.floors)
        return saved.requiredId
    }

    @Transactional(readOnly = true)
    fun findAll(): List<BuildingResponse> {
        val buildings = repository.findAll(SortUtils.orderByCreatedAtDesc)
        val fileMap =
            fileService.getFileMapByIds(buildings) {
                listOfNotNull(it.drawingFileId, it.thumbnailFileId)
            }
        val floorMap = floorService.findAllByFacilities(buildings)
        return buildings
            .map {
                BuildingResponse(
                    it.toResponse(
                        fileMap[it.drawingFileId],
                        fileMap[it.thumbnailFileId],
                    ),
                    floorMap[it],
                )
            }.toList()
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): BuildingResponse {
        val building = facilityService.findById(id)
        val floorResponses = floorService.findAllByFacility(building)

        return BuildingResponse(
            building.toResponse(
                fileService.getFileResponse(building.drawingFileId),
                fileService.getFileResponse(building.thumbnailFileId),
            ),
            floorResponses,
        )
    }

    @Transactional
    fun update(
        id: Long,
        request: BuildingUpdateRequest,
    ) {
        val building = findBuilding(id)

        facilityService.update(id, request.facility)
        floorService.update(building, request.floors)
    }

    @Transactional
    fun putUpdate(
        id: Long,
        request: BuildingUpdateRequest,
    ) {
        val building = findBuilding(id)

        facilityService.putUpdate(id, request.facility)
        floorService.update(building, request.floors)
    }

    private fun findBuilding(id: Long): Building =
        repository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_BUILDING, id)

    @Transactional
    fun delete(id: Long) {
        val building = facilityService.findById(id)
        floorService.delete(building)
        facilityService.deleteFacility(id)
    }
}
