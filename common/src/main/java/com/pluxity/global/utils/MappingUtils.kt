package com.pluxity.global.utils

import com.pluxity.facility.Facility
import com.pluxity.facility.dto.FacilityResponse
import com.pluxity.facility.dto.toResponse
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.service.FileService
import java.util.function.Function
import java.util.stream.Stream

object MappingUtils {
    fun mapWithFiles(
        entities: List<Facility>,
        fileService: FileService,
    ): List<FacilityResponse> {
        val fileMap =
            getFileMapByIds(
                entities,
                { entity -> Stream.of(entity.drawingFileId, entity.thumbnailFileId) },
                fileService,
            )
        return entities.map { entity ->
            entity.toResponse(
                fileMap[entity.drawingFileId],
                fileMap[entity.thumbnailFileId],
            )
        }
    }

    fun <T> getFileMapByIds(
        list: List<T>,
        fileIdGetter: Function<T, Stream<Long>>,
        fileService: FileService,
    ): Map<Long, FileResponse> {
        // file Id 추출
        val fileIds =
            list
                .flatMap { fileIdGetter.apply(it).toList() }
                .filterNotNull()
                .distinct()

        if (fileIds.isEmpty()) {
            return emptyMap()
        }

        return fileService
            .getFiles(fileIds)
            .associateBy { it.id!! }
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
