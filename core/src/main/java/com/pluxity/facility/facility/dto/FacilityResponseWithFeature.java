package com.pluxity.facility.facility.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.facility.Facility;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.global.response.BaseResponse;

public record FacilityResponseWithFeature(
    Long id,
    String code,
    String name,
    String description,
    FileResponse drawing,
    FileResponse thumbnail,
    @JsonUnwrapped BaseResponse baseResponse
) {
    public static FacilityResponseWithFeature from(Facility facility, FileResponse drawing, FileResponse thumbnail) {
        return new FacilityResponseWithFeature(
            facility.getId(),
            facility.getCode(),
            facility.getName(),
            facility.getDescription(),
            drawing,
            thumbnail,
            BaseResponse.of(facility)
        );
    }
}
