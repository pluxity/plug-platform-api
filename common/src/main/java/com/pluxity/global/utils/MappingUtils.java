package com.pluxity.global.utils;

import com.pluxity.facility.Facility;
import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MappingUtils {

    public static List<FacilityResponse> mapWithFiles(
            List<? extends Facility> entities, FileService fileService) {
        Map<Long, FileResponse> fileMap =
                getFileMapByIds(
                        entities, e -> Stream.of(e.getDrawingFileId(), e.getThumbnailFileId()), fileService);
        return entities.stream()
                .map(
                        v ->
                                FacilityResponse.from(
                                        v, fileMap.get(v.getDrawingFileId()), fileMap.get(v.getThumbnailFileId())))
                .toList();
    }

    public static <T> Map<Long, FileResponse> getFileMapByIds(
            List<T> list, Function<T, Stream<Long>> fileIdGetter, FileService fileService) {
        // file Id 추출
        List<Long> fileIds = list.stream().flatMap(fileIdGetter).filter(Objects::nonNull).toList();
        // file id list로 파일 정보 조회 후 id, fileResponse 형태의 map 생성
        return fileService.getFiles(fileIds).stream()
                .collect(Collectors.toMap(FileResponse::id, f -> f));
    }

    public static <T> List<T> makeCategoryTree(
            List<T> list,
            Function<T, Long> idGetter,
            Function<T, Long> parentIdGetter,
            Function<T, List<T>> childrenGetter) {
        // id, category 형태의 map 생성
        Map<Long, T> map = list.stream().collect(Collectors.toMap(idGetter, c -> c));
        List<T> roots = new ArrayList<>();
        for (T item : list) {
            Long parentId = parentIdGetter.apply(item);
            if (parentId == null) { // 루트 노드인 경우
                roots.add(item);
            } else {
                T parent = map.get(parentId);
                childrenGetter.apply(parent).add(item);
            }
        }
        return roots;
    }
}
