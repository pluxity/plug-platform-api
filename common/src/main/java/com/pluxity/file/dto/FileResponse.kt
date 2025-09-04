package com.pluxity.file.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.pluxity.file.entity.FileEntity
import com.pluxity.global.response.BaseResponse
import com.pluxity.global.response.toBaseResponse

data class FileResponse(
    var id: Long? = null,
    var url: String? = null,
    var originalFileName: String? = null,
    var contentType: String? = null,
    var fileStatus: String? = null,
    @field:JsonUnwrapped var baseResponse: BaseResponse? = null,
)

fun FileEntity.toFileResponse(url: String?) =
    FileResponse(
        id = this.id,
        url = url ?: this.filePath,
        originalFileName = this.originalFileName,
        contentType = this.contentType,
        fileStatus = this.fileStatus.toString(),
        baseResponse = this.toBaseResponse(),
    )
