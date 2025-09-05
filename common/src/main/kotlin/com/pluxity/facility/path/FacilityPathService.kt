package com.pluxity.facility.path

import com.pluxity.facility.Facility
import com.pluxity.global.constant.ErrorCode.NOT_FOUND_FACILITY_PATH
import com.pluxity.global.exception.CustomException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FacilityPathService(
    private val facilityPathRepository: FacilityPathRepository,
) {
    @Transactional(readOnly = true)
    fun findById(id: Long): FacilityPath =
        facilityPathRepository.findByIdOrNull(id)
            ?: throw CustomException(NOT_FOUND_FACILITY_PATH, id)

    @Transactional
    fun update(
        pathId: Long,
        name: String?,
        type: String?,
        path: String?,
    ) {
        val facilityPath = findById(pathId)
        name?.takeIf { it.isNotBlank() }?.let { facilityPath.updateName(it) }
        type?.takeIf { it.isNotBlank() }?.let { facilityPath.updatePathType(PathType.from(it)) }
        path?.takeIf { it.isNotBlank() }?.let { facilityPath.updatePath(it) }
    }

    @Transactional
    fun save(
        facility: Facility,
        name: String,
        type: String,
        path: String,
    ) {
        facilityPathRepository.save(
            FacilityPath(
                facility = facility,
                name = name,
                pathType = PathType.from(type),
                path = path,
            ),
        )
    }

    @Transactional
    fun delete(pathId: Long) {
        facilityPathRepository.delete(findById(pathId))
    }
}
