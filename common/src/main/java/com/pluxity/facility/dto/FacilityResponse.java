package com.pluxity.facility.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.Facility;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.global.response.BaseResponse;
import java.util.List;
import lombok.Builder;

@Builder
public record FacilityResponse(
        Long id,
        String code,
        String name,
        String description,
        FileResponse drawing,
        FileResponse thumbnail,
        List<FacilityPathResponse> paths,
        Double lon,
        Double lat,
        String locationMeta,
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
                facility.getPaths().stream().map(FacilityPathResponse::from).toList(),
                facility.getPosition() != null ? facility.getPosition().getLon() : null,
                facility.getPosition() != null ? facility.getPosition().getLat() : null,
                facility.getPosition() != null ? facility.getPosition().getLocationMeta() : null,
                BaseResponse.of(facility));
    }
}
