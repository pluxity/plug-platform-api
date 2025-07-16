package com.pluxity.facility;

import com.pluxity.building.BuildingService;
import com.pluxity.facility.dto.FacilityAllResponse;
import com.pluxity.facility.dto.FacilityDrawingUpdateRequest;
import com.pluxity.panorama.PanoramaService;
import com.pluxity.station.StationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityApiService {

    private final BuildingService buildingService;
    private final PanoramaService panoramaService;
    private final StationService stationService;
    private final FacilityService facilityService;

    protected FacilityAllResponse findAll() {
        return FacilityAllResponse.from(
                buildingService.findAllFacilities(), stationService.findAllFacilities());
    }

    public void updateDrawingFile(Long id, FacilityDrawingUpdateRequest request) {
        facilityService.updateDrawingFile(id, request.drawingFileId(), request.comment());
    }
}
