package com.pluxity.facility.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.Facility;
import com.pluxity.feature.dto.FeatureResponseWithoutAsset;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.global.response.BaseResponse;
import java.util.List;
import java.util.stream.Collectors;

public record FacilityResponseWithFeature(
        Long id,
        String code,
        String name,
        String description,
        FileResponse drawing,
        FileResponse thumbnail,
        @JsonUnwrapped BaseResponse baseResponse) {

    public static FacilityResponseWithFeature from(
            Facility facility, FileResponse drawing, FileResponse thumbnail) {

        return new FacilityResponseWithFeature(
                facility.getId(),
                facility.getCode(),
                facility.getName(),
                facility.getDescription(),
                drawing != null ? drawing : FileResponse.empty(),
                thumbnail != null ? thumbnail : FileResponse.empty(),
                BaseResponse.of(facility));
    }

    public static List<FeatureResponseWithoutAsset> getFeatureResponses(Facility facility) {
        return facility.getFeatures().stream()
                .map(FeatureResponseWithoutAsset::from)
                .collect(Collectors.toList());
    }

    public static List<FeatureResponseWithoutAsset> getFeatureResponsesExcludingLabel3D(
            Facility facility, List<String> label3DFeatureIds) {
        return facility.getFeatures().stream()
                .filter(feature -> !label3DFeatureIds.contains(feature.getId()))
                .map(FeatureResponseWithoutAsset::from)
                .collect(Collectors.toList());
    }
}
