package com.pluxity.domains.station.dto;

import com.pluxity.facility.facility.dto.FacilityUpdateRequest;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SasangStationUpdateRequest {

    private FacilityUpdateRequest facility;

    private String externalCode;
    private String route;
    private String subway;
    private Integer platformCount;
    private Boolean isTransferStation;
}
