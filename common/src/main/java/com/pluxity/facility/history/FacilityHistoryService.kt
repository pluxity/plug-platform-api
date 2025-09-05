package com.pluxity.facility.history

import com.pluxity.facility.dto.FacilityHistoryResponse
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.service.FileService
import com.pluxity.global.utils.MappingUtils
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Function
import java.util.stream.Stream

@Service
@RequiredArgsConstructor
class FacilityHistoryService {
    private val facilityHistoryRepository: FacilityHistoryRepository? = null
    private val fileService: FileService? = null

    @Transactional
    fun save(fileId: Long?, facilityId: Long?, comment: String?) {
        facilityHistoryRepository!!.save<FacilityHistory?>(
            FacilityHistory.builder().fileId(fileId).facilityId(facilityId).comment(comment).build()
        )
    }

    @Transactional(readOnly = true)
    fun findByFacilityId(facilityId: Long?): MutableList<FacilityHistoryResponse?> {
        val histories =
            facilityHistoryRepository!!.findByFacilityIdOrderByCreatedAtDesc(facilityId)
        val fileMap: MutableMap<Long?, FileResponse?> =
            MappingUtils.getFileMapByIds<FacilityHistory?>(
                histories, Function { v: FacilityHistory? -> Stream.of<Long>(v!!.getFileId()) }, fileService!!
            )
        return histories.stream()
            .map<FacilityHistoryResponse?> { v: FacilityHistory? -> FacilityHistoryResponse.Companion.from(v, fileMap.get(v!!.getFileId())) }
            .toList()
    }
}
