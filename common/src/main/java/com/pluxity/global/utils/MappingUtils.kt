package com.pluxity.global.utils

import com.pluxity.facility.Facility
import com.pluxity.facility.dto.FacilityResponse
import com.pluxity.facility.dto.toResponse
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.service.FileService
import java.util.function.Function

object MappingUtils {
    fun mapWithFiles(
        entities: List<Facility>,
        fileService: FileService,
    ): List<FacilityResponse> {
        val fileMap =
            fileService.getFileMapByIds(entities) {
                listOfNotNull(it.drawingFileId, it.thumbnailFileId)
            }
        return entities.map { entity ->
            entity.toResponse(
                fileMap[entity.drawingFileId],
                fileMap[entity.thumbnailFileId],
            )
        }
    }

    fun <T> makeCategoryTree(
        list: List<T>,
        idGetter: Function<T, Long?>,
        parentIdGetter: Function<T, Long?>,
        childrenGetter: Function<T, MutableList<T>>,
    ): List<T> {
        val map =
            list
                .mapNotNull { item ->
                    idGetter.apply(item)?.let { id -> id to item }
                }.toMap()
        val roots = mutableListOf<T>()
        for (item in list) {
            val parentId = parentIdGetter.apply(item)
            if (parentId == null) {
                roots.add(item)
            } else {
                map[parentId]?.let { parent ->
                    childrenGetter.apply(parent).add(item)
                }
            }
        }
        return roots
    }

    fun <ID, E> findByIdIfExists(
        id: ID?,
        getter: Function<ID, E>,
    ): E? = id?.let(getter::apply)
}

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
