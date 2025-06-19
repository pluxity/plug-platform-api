package com.pluxity.domains.sasang.station.dto; // Updated package

import com.pluxity.facility.facility.dto.FacilityUpdateRequest;
import com.pluxity.facility.floor.dto.FloorRequest;
import java.util.List;

public record SasangStationUpdateRequest(
        FacilityUpdateRequest facility,
        List<FloorRequest> floors,
        List<Long> lineIds,
        String externalCode) {}
