package com.pluxity.global.utils

import com.pluxity.facility.Facility
import com.pluxity.facility.dto.FacilityResponse
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.service.FileService
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

object MappingUtils {
    fun mapWithFiles(
        entities: MutableList<out Facility?>, fileService: FileService
    ): MutableList<FacilityResponse?> {
        val fileMap =
            MappingUtils.getFileMapByIds(
                entities, { e: Facility? -> Stream.of<Long?>(e!!.drawingFileId, e.thumbnailFileId) }, fileService
            )
        return entities.stream()
            .map<FacilityResponse> { v: Facility? ->
                FacilityResponse.from(
                    v!!, fileMap.get(v.drawingFileId), fileMap.get(v.thumbnailFileId)
                )
            }
            .collect(Collectors.toList())
    }

    fun <T> getFileMapByIds(
        list: MutableList<T?>, fileIdGetter: Function<T?, Stream<Long?>?>?, fileService: FileService
    ): MutableMap<Long?, FileResponse?> {
        // file Id 추출
        val fileIds = list.stream().flatMap<Long?>(fileIdGetter).filter { obj: Long? -> Objects.nonNull(obj) }.toList()
        // file id list로 파일 정보 조회 후 id, fileResponse 형태의 map 생성
        return fileService.getFiles(fileIds).stream()
            .collect(Collectors.toMap(FileResponse::id, Function { f: FileResponse? -> f }))
    }

    fun <T> makeCategoryTree(
        list: MutableList<T?>,
        idGetter: Function<T?, Long?>,
        parentIdGetter: Function<T?, Long?>,
        childrenGetter: Function<T?, MutableList<T?>?>
    ): MutableList<T?> {
        // id, category 형태의 map 생성
        val map = list.stream().collect(Collectors.toMap(idGetter, Function { c: T? -> c }))
        val roots: MutableList<T?> = ArrayList<T?>()
        for (item in list) {
            val parentId = parentIdGetter.apply(item)
            if (parentId == null) { // 루트 노드인 경우
                roots.add(item)
            } else {
                if (map.containsKey(parentId)) {
                    val parent = map.get(parentId)
                    childrenGetter.apply(parent)!!.add(item)
                }
            }
        }
        return roots
    }

    fun <ID, E> findByIdIfExists(id: ID?, getter: Function<ID?, E?>): E? {
        return Optional.ofNullable<ID?>(id).map<E?>(getter).orElse(null)
    }
}
