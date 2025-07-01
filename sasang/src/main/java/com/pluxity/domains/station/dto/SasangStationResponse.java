package com.pluxity.domains.station.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.pluxity.domains.station.SasangStationDetails;
import com.pluxity.facility.facility.dto.FacilityResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SasangStationResponse {

    @JsonUnwrapped
    private FacilityResponse facilityResponse;

    private String route;
    private String subway;
    private String externalCode;
    private Integer platformCount;
    private Boolean isTransferStation;

    public static SasangStationResponse from(FacilityResponse facilityResponse, SasangStationDetails details) {
        return SasangStationResponse.builder()
                .facilityResponse(facilityResponse)
                .route(details.getRoute())
                .subway(details.getSubway())
                .externalCode(details.getExternalCode())
                .platformCount(details.getPlatformCount())
                .isTransferStation(details.getIsTransferStation())
                .build();
    }
}
