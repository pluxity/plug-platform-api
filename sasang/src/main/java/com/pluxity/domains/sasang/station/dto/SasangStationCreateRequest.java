package com.pluxity.domains.sasang.station.dto;

import com.pluxity.facility.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.floor.dto.FloorRequest;
import java.util.List;

public record SasangStationCreateRequest(
        FacilityCreateRequest facility,
        List<FloorRequest> floors,
        List<Long> lineIds,
        String externalCode,
        String route // Added field
) {}
