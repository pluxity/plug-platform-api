package com.pluxity.facility.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityType;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.global.response.BaseResponse;
import java.util.List;
import lombok.Builder;
import org.jetbrains.annotations.Nullable;

@Builder
public record FacilityResponse(
        Long id,
        String code,
        String name,
        String description,
        FacilityType type,
        FileResponse drawing,
        FileResponse thumbnail,
        List<FacilityPathResponse> paths,
        @Nullable Double lon,
        @Nullable Double lat,
        @Nullable String locationMeta,
        @JsonUnwrapped BaseResponse baseResponse) {
    public static FacilityResponse from(
            Facility facility, FileResponse drawing, FileResponse thumbnail) {
        return new FacilityResponse(
                facility.getId(),
                facility.getCode(),
                facility.getName(),
                facility.getDescription(),
                facility.getFacilityType(),
                drawing != null ? drawing : new FileResponse(null, null, null, null, null, null),
                thumbnail != null ? thumbnail : new FileResponse(null, null, null, null, null, null),
                facility.getPaths().stream().map(FacilityPathResponse::from).toList(),
                facility.getPosition() != null ? facility.getPosition().getLon() : null,
                facility.getPosition() != null ? facility.getPosition().getLat() : null,
                facility.getPosition() != null ? facility.getPosition().getLocationMeta() : null,
                BaseResponse.of(facility));
    }
}
