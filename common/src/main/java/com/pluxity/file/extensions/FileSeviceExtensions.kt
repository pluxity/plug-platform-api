package com.pluxity.file.extensions

import com.pluxity.file.dto.FileResponse
import com.pluxity.file.service.FileService

fun <T> FileService.getFileMapById(
    items: List<T>,
    idExtractor: (T) -> Long?,
): Map<Long, FileResponse> {
    val fileIds = items.mapNotNull(idExtractor)
    return if (fileIds.isEmpty()) {
        emptyMap()
    } else {
        getFiles(fileIds).associateBy { it.id!! }
    }
}

fun <T> FileService.getFileMapByIds(
    items: List<T>,
    idExtractor: (T) -> List<Long?>,
): Map<Long, FileResponse> {
    val fileIds = items.flatMap(idExtractor).filterNotNull()
    return if (fileIds.isEmpty()) {
        emptyMap()
    } else {
        getFiles(fileIds).associateBy { it.id!! }
    }
}
