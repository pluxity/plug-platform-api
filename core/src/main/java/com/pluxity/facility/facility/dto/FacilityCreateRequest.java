package com.pluxity.facility.facility.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.pluxity.facility.facility.FacilityType;
import com.pluxity.facility.facility.dto.details.BuildingDetailsDto;
import com.pluxity.facility.facility.dto.details.FacilityDetailsDto;
import com.pluxity.facility.facility.dto.details.PanoramaDetailsDto;
import com.pluxity.facility.facility.dto.details.StationDetailsDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record FacilityCreateRequest(
    @NotNull FacilityType facilityType,
    @NotBlank String name,
    @NotBlank String code,
    String description,
    Long drawingFileId,
    Long thumbnailFileId,
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "facilityType"
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = StationDetailsDto.class, name = "STATION"),
        @JsonSubTypes.Type(value = BuildingDetailsDto.class, name = "BUILDING"),
        @JsonSubTypes.Type(value = PanoramaDetailsDto.class, name = "PANORAMA")
    })
    FacilityDetailsDto details
) {
}
