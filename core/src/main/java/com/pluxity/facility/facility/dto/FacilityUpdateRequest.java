package com.pluxity.facility.facility.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.pluxity.facility.facility.dto.details.BuildingDetailsDto;
import com.pluxity.facility.facility.dto.details.FacilityDetailsDto;
import com.pluxity.facility.facility.dto.details.PanoramaDetailsDto;
import com.pluxity.facility.facility.dto.details.StationDetailsDto;
import jakarta.validation.constraints.Size;

public record FacilityUpdateRequest(
    @Size(max = 50, message = "이름은 최대 50자까지 입력 가능합니다.") String name,
    @Size(max = 20, message = "코드는 최대 20자까지 입력 가능합니다.") String code,
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
