package com.pluxity.facility.dto

import com.pluxity.facility.history.FacilityHistory
import com.pluxity.file.dto.FileResponse

data class FacilityHistoryResponse(
    val id: Long,
    val comment: String,
    val createdAt: String,
    val createdBy: String,
    val file: FileResponse,
)

fun FacilityHistory.toHistoryResponse(file: FileResponse): FacilityHistoryResponse =
    FacilityHistoryResponse(
        id = this.id!!,
        comment = this.comment,
        createdAt = this.createdAt.toString(),
        createdBy = this.createdBy.toString(),
        file = file,
    )
