package com.pluxity.facility.facility.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.facility.Facility;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.global.response.BaseResponse;
import lombok.Builder;

@Builder
public record FacilityResponse(
        Long id,
        String code,
        String name,
        String description,
        FileResponse drawing,
        FileResponse thumbnail,
        @JsonUnwrapped BaseResponse baseResponse) {
    public static FacilityResponse from(
            Facility facility, FileResponse drawing, FileResponse thumbnail) {
        return new FacilityResponse(
                facility.getId(),
                facility.getCode(),
                facility.getName(),
                facility.getDescription(),
                drawing != null ? drawing : FileResponse.empty(),
                thumbnail != null ? thumbnail : FileResponse.empty(),
                BaseResponse.of(facility));
    }
}
