package com.pluxity.facility.service;

import com.pluxity.facility.dto.*;
import com.pluxity.facility.entity.Facility;
import com.pluxity.facility.entity.Station;
import com.pluxity.facility.repository.StationRepository;
import com.pluxity.facility.strategy.FloorStrategy;
import com.pluxity.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StationService {

    private final FileService fileService;
    private final FacilityService facilityService;
    private final FloorStrategy floorStrategy;
    private final StationRepository stationRepository;

    @Transactional
    public Long save(StationCreateRequest request) {

        Station station = Station.builder()
                .name(request.facility().name())
                .description(request.facility().description())
                .build();

        Facility saved = facilityService.save(station, request.facility());

        if(request.floors() != null) {
            for (FloorRequest floorRequest : request.floors()) {
                floorStrategy.save(saved, floorRequest);
            }
        }

        return saved.getId();
    }

    @Transactional(readOnly = true)
    public List<StationResponse> findAll() {
        return stationRepository.findAll().stream()
                .map(station -> StationResponse.builder()
                        .facility(FacilityResponse.from(station, fileService.getFileResponse(station.getDrawingFileId()), fileService.getFileResponse(station.getThumbnailFileId())))
                        .build())
                .toList();

    }

    @Transactional(readOnly = true)
    public StationResponse findById(Long id) {
        Station station = (Station) facilityService.findById(id);
        List<FloorResponse> floorResponse = floorStrategy.findAllByFacility(station);

        return StationResponse.builder()
                .facility(FacilityResponse.from(station, fileService.getFileResponse(station.getDrawingFileId()), fileService.getFileResponse(station.getThumbnailFileId())))
                .floors(floorResponse)
                .build();
    }

    @Transactional(readOnly = true)
    public List<FacilityHistoryResponse> findFacilityHistories(Long id) {
        return facilityService.findFacilityHistories(id);
    }

    @Transactional
    public void update(Long id, StationUpdateRequest request) {
        facilityService.update(id, Station.builder()
                .name(request.name())
                .description(request.description())
                .build());
    }

    @Transactional
    public void delete(Long id) {
        Facility station = facilityService.findById(id);
        floorStrategy.delete(station);
        facilityService.deleteFacility(id);
    }

}
