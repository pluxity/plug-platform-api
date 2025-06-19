package com.pluxity.domains.sasang.station.dto; // Updated package

import com.pluxity.facility.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse;
import com.pluxity.facility.station.dto.StationResponse;
import java.util.List;

public record SasangStationResponse(
        FacilityResponse facility,
        List<FloorResponse> floors,
        List<Long> lineIds,
        List<String> featureIds,
        String route,
        String externalCode) {

    public static SasangStationResponse of(StationResponse stationResponse, String externalCode) {
        return new SasangStationResponse(
                stationResponse.facility(),
                stationResponse.floors(),
                stationResponse.lineIds(),
                stationResponse.featureIds(),
                stationResponse.route(),
                externalCode);
    }
}
