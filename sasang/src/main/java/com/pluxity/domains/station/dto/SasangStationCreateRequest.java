package com.pluxity.domains.station.dto;

import com.pluxity.facility.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.floor.dto.FloorRequest;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SasangStationCreateRequest {
    private FacilityCreateRequest facility;
    private List<Long> lineIds;
    private List<FloorRequest> floors;
    private String externalCode;
    private String route;
    private String subway;
    private Integer platformCount;
    private Boolean isTransferStation;
}
