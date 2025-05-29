package com.pluxity.facility.facility.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.facility.Facility;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.global.response.BaseResponse;
import java.util.List;
import java.util.stream.Collectors;

public record FacilityResponseWithFeature(
        Long id,
        String code,
        String name,
        String description,
        List<FeatureResponse> features,
        FileResponse drawing,
        FileResponse thumbnail,
        @JsonUnwrapped BaseResponse baseResponse) {

    public static FacilityResponseWithFeature from(
            Facility facility, FileResponse drawing, FileResponse thumbnail) {

        List<FeatureResponse> featureResponses =
                facility.getFeatures().stream().map(FeatureResponse::from).collect(Collectors.toList());

        return new FacilityResponseWithFeature(
                facility.getId(),
                facility.getCode(),
                facility.getName(),
                facility.getDescription(),
                featureResponses,
                drawing != null ? drawing : FileResponse.empty(),
                thumbnail != null ? thumbnail : FileResponse.empty(),
                BaseResponse.of(facility));
    }
}
