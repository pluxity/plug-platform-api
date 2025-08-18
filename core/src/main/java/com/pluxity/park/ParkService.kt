package com.pluxity.park

import com.pluxity.facility.FacilityService
import com.pluxity.facility.dto.FacilityResponse
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.MappingUtils
import com.pluxity.global.utils.SortUtils
import com.pluxity.park.dto.ParkCreateRequest
import com.pluxity.park.dto.ParkResponse
import com.pluxity.park.dto.ParkUpdateRequest
import lombok.RequiredArgsConstructor
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Stream

@Service
@RequiredArgsConstructor
class ParkService(
    private val fileService: FileService,
    private val facilityService: FacilityService,
    private val parkRepository: ParkRepository,
) {
    @Transactional
    fun save(request: ParkCreateRequest): Long {
        val park: Park = Park(request.facility.name, request.facility.description, request.boundary)
        val saved = facilityService.save(park, request.facility)
        return saved.id
    }

    @Transactional(readOnly = true)
    fun findAll(): List<ParkResponse> {
        val parks = parkRepository.findAll(SortUtils.getOrderByCreatedAtDesc())
        val fileMap =
            MappingUtils.getFileMapByIds(
                parks,
                { v: Park -> Stream.of(v.drawingFileId, v.thumbnailFileId) },
                fileService,
            )

        return parks.map {
            ParkResponse(
                FacilityResponse.from(
                    it,
                    fileMap[it.drawingFileId],
                    fileMap[it.thumbnailFileId],
                ),
                it.boundary,
            )
        }
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): ParkResponse {
        val park = facilityService.findById(id) as Park

        return ParkResponse(
            FacilityResponse.from(
                park,
                fileService.getFileResponse(park.drawingFileId),
                fileService.getFileResponse(park.thumbnailFileId),
            ),
            park.boundary,
        )
    }

    private fun findPark(id: Long): Park {
        return parkRepository
            .findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_PARK, id)
    }

    @Transactional
    fun update(
        id: Long,
        request: ParkUpdateRequest,
    ) {
        val park = findPark(id)
        facilityService.putUpdate(id, request.facility)
        park.updateBoundary(request.boundary)
    }

    @Transactional
    fun delete(id: Long) {
        findPark(id)
        facilityService.deleteFacility(id)
    }
}
