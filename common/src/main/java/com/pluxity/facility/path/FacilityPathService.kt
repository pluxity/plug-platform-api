package com.pluxity.facility.path

import com.pluxity.facility.Facility
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils
import java.util.function.Supplier

@Service
@RequiredArgsConstructor
class FacilityPathService {
    private val facilityPathRepository: FacilityPathRepository? = null

    @Transactional(readOnly = true)
    fun findById(id: Long): FacilityPath {
        return facilityPathRepository!!
            .findById(id)
            .orElseThrow<CustomException?>(Supplier { CustomException(ErrorCode.NOT_FOUND_FACILITY_PATH, id) })
    }

    @Transactional
    fun update(pathId: Long, name: String?, type: String?, path: String?) {
        val facilityPath = findById(pathId)
        if (StringUtils.hasText(name)) {
            facilityPath.updateName(name)
        }
        if (StringUtils.hasText(type)) {
            facilityPath.updatePathType(PathType.Companion.from(type))
        }
        if (StringUtils.hasText(path)) {
            facilityPath.updatePath(path)
        }
    }

    @Transactional
    fun save(facility: Facility?, name: String?, type: String?, path: String?) {
        facilityPathRepository!!.save<FacilityPath?>(
            FacilityPath.builder()
                .facility(facility)
                .name(name)
                .pathType(PathType.Companion.from(type))
                .path(path)
                .build()
        )
    }

    @Transactional
    fun delete(pathId: Long) {
        facilityPathRepository!!.delete(findById(pathId))
    }
}
