package com.pluxity.facility.history

import com.pluxity.facility.dto.FacilityHistoryResponse
import com.pluxity.facility.dto.toHistoryResponse
import com.pluxity.file.extensions.getFileMapById
import com.pluxity.file.service.FileService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FacilityHistoryService(
    private val facilityHistoryRepository: FacilityHistoryRepository,
    private val fileService: FileService,
) {
    @Transactional
    fun save(
        fileId: Long,
        facilityId: Long,
        comment: String,
    ) {
        facilityHistoryRepository.save(
            FacilityHistory(
                fileId = fileId,
                facilityId = facilityId,
                comment = comment,
            ),
        )
    }

    @Transactional(readOnly = true)
    fun findByFacilityId(facilityId: Long): List<FacilityHistoryResponse> {
        val histories = facilityHistoryRepository.findByFacilityIdOrderByCreatedAtDesc(facilityId)
        val fileMap = fileService.getFileMapById(histories) { it.fileId }
        return histories.map { history ->
            history.toHistoryResponse(fileMap.getValue(history.fileId))
        }
    }
}
