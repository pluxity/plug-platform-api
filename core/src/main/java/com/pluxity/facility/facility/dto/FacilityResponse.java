package com.pluxity.facility.facility.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityType;
import com.pluxity.facility.facility.dto.details.FacilityDetailsDto;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.global.response.BaseResponse;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FacilityResponse(
    Long id,
    FacilityType facilityType,
    String name,
    String code,
    String description,
    FileResponse drawingFile,
    FileResponse thumbnailFile,
    FacilityDetailsDto details,
    BaseResponse baseResponse
) {
    public static FacilityResponse from(
        Facility facility,
        FileResponse drawingFile,
        FileResponse thumbnailFile,
        FacilityDetailsDto details
    ) {
        return new FacilityResponse(
            facility.getId(),
            facility.getType(),
            facility.getName(),
            facility.getCode(),
            facility.getDescription(),
            drawingFile,
            thumbnailFile,
            details,
            BaseResponse.of(facility)
        );
    }
}
