package com.pluxity.utils;

import com.pluxity.facility.Facility;
import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FacilityMappingUtil {

    public static List<FacilityResponse> mapWithFiles(
            List<? extends Facility> entities, FileService fileService) {
        List<Long> fileIds =
                entities.stream()
                        .flatMap(e -> Stream.of(e.getDrawingFileId(), e.getThumbnailFileId()))
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        Map<Long, FileResponse> fileMap =
                fileService.getFiles(fileIds).stream().collect(Collectors.toMap(FileResponse::id, f -> f));

        return entities.stream()
                .map(
                        v ->
                                FacilityResponse.from(
                                        v, fileMap.get(v.getDrawingFileId()), fileMap.get(v.getThumbnailFileId())))
                .toList();
    }
}
