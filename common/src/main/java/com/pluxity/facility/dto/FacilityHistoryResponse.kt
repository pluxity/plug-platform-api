package com.pluxity.facility.dto

import com.pluxity.facility.history.FacilityHistory
import com.pluxity.file.dto.FileResponse

@JvmRecord
data class FacilityHistoryResponse(val id: Long?, val comment: String?, val createdAt: String?, val createdBy: String?, val file: FileResponse?) {
    companion object {
        fun from(entity: FacilityHistory, file: FileResponse?): FacilityHistoryResponse {
            return FacilityHistoryResponse(
                entity.getId(),
                entity.getComment(),
                entity.createdAt.toString(),
                entity.createdBy,
                file
            )
        }
    }
}
