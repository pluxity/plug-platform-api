package com.pluxity.facility.station;

import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityService;
import com.pluxity.facility.facility.dto.FacilityHistoryResponse;
import com.pluxity.facility.facility.dto.FacilityResponse;
import com.pluxity.facility.facility.dto.FacilityResponseWithFeature;
import com.pluxity.facility.floor.dto.FloorRequest;
import com.pluxity.facility.floor.dto.FloorResponse;
import com.pluxity.facility.line.Line;
import com.pluxity.facility.line.LineService;
import com.pluxity.facility.station.dto.StationCreateRequest;
import com.pluxity.facility.station.dto.StationResponse;
import com.pluxity.facility.station.dto.StationResponseWithFeature;
import com.pluxity.facility.station.dto.StationUpdateRequest;
import com.pluxity.facility.strategy.FloorStrategy;
import com.pluxity.feature.dto.FeatureResponseWithoutAsset;
import com.pluxity.feature.entity.Feature;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import com.pluxity.label3d.Label3DRepository;
import com.pluxity.label3d.entity.Label3D; // Added import for Label3D
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StationService {

    private final FileService fileService;
    private final FacilityService facilityService;
    private final FloorStrategy floorStrategy;
    private final StationRepository stationRepository;
    private final LineService lineService;
    private final Label3DRepository label3DRepository;

    @Transactional
    public Long save(StationCreateRequest request) {
        // Create Station, which internally creates its Facility
        Station station = new Station(
            request.facility().name(),
            request.facility().description(),
            request.route() // Assuming StationCreateRequest has getRoute()
        );
        // stationRepository.save(station); // Explicitly save station if not handled by cascade from FacilityService or transaction boundary

        // Pass the Facility from Station to FacilityService to update details if needed
        Facility savedFacility = facilityService.save(station.getFacility(), request.facility());
        stationRepository.save(station); // Ensure station itself is saved to get its ID

        if (request.floors() != null) {
            for (FloorRequest floorRequest : request.floors()) {
                floorStrategy.save(station.getFacility(), floorRequest);
            }
        }

        if (request.lineIds() != null && !request.lineIds().isEmpty()) {
            for (Long lineId : request.lineIds()) {
                Line line = lineService.findLineById(lineId);
                station.addLine(line);
            }
        }
        // Return Station ID as per typical save operations for the primary entity being created
        return station.getId();
    }

    @Transactional(readOnly = true)
    public List<StationResponse> findAll() {
        return stationRepository.findAll().stream()
                .map(
                        station -> {
                            Facility facility = station.getFacility();
                            List<Long> lineIds =
                                    station.getStationLines().stream()
                                            .map(stationLine -> stationLine.getLine().getId())
                                            .collect(Collectors.toList());

                            List<FloorResponse> floorResponse = floorStrategy.findAllByFacility(facility);
                            List<String> featureIds = facility.getFeatures().stream().map(Feature::getId).toList();
                            // FacilityResponseWithFeature.getFeatureResponses(facility); // This method seems to be static and returns a List, not modifying input

                            return StationResponse.builder()
                                    .facility(
                                            FacilityResponse.from(
                                                    facility,
                                                    fileService.getFileResponse(facility.getDrawingFileId()),
                                                    fileService.getFileResponse(facility.getThumbnailFileId())))
                                    .floors(floorResponse)
                                    .lineIds(lineIds)
                                    .featureIds(featureIds)
                                    .route(station.getRoute())
                                    .subway(station.getSubway())
                                    .build();
                        })
                .toList();
    }

    @Transactional(readOnly = true)
    public StationResponse findById(Long id) {
        Station station = findStationById(id); // Use local method to fetch Station
        Facility facility = station.getFacility();
        List<FloorResponse> floorResponse = floorStrategy.findAllByFacility(facility);
        List<String> featureIds = facility.getFeatures().stream().map(Feature::getId).toList();

        List<Long> lineIds =
                station.getStationLines().stream()
                        .map(stationLine -> stationLine.getLine().getId())
                        .collect(Collectors.toList());

        return StationResponse.builder()
                .facility(
                        FacilityResponse.from(
                                facility,
                                fileService.getFileResponse(facility.getDrawingFileId()),
                                fileService.getFileResponse(facility.getThumbnailFileId())))
                .floors(floorResponse)
                .lineIds(lineIds)
                .featureIds(featureIds)
                .route(station.getRoute())
                .subway(station.getSubway())
                .build();
    }

    @Transactional(readOnly = true)
    public Station findStationById(Long id) {
        return stationRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new CustomException("Station not found", HttpStatus.NOT_FOUND, "해당 역을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<FacilityHistoryResponse> findFacilityHistories(Long id) {
        // Assuming this should fetch histories for the Facility associated with the Station
        Station station = findStationById(id);
        return facilityService.findFacilityHistories(station.getFacility().getId());
    }

    @Transactional
    public void update(Long id, StationUpdateRequest request) {
        Station station = findStationById(id);
        Facility facility = station.getFacility();

        facilityService.update(facility.getId(), request.facility());

        if (request.floors() != null) {
            floorStrategy.delete(facility);
            for (FloorRequest floorRequest : request.floors()) {
                floorStrategy.save(facility, floorRequest);
            }
        }

        // Update station-specific fields like route if they are in StationUpdateRequest
        // For example: station.updateRoute(request.getRoute());

        if (request.lineIds() != null) {
            // Manage clearing and adding lines carefully
            // This simple clear and add might lose other StationLine properties if any
            station.getStationLines().clear(); // Consider a more sophisticated update if needed
            for (Long lineId : request.lineIds()) {
                Line line = lineService.findLineById(lineId);
                station.addLine(line);
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        Station station = findStationById(id);
        // Deleting the station should cascade to Facility due to orphanRemoval=true
        // floorStrategy.delete(station.getFacility()); // This might be handled by cascade from Facility or needs to be done before facility removal
        stationRepository.delete(station);
        // facilityService.deleteFacility(station.getFacility().getId()); // Should be redundant if cascade is set up correctly
    }

    @Transactional
    public void addLineToStation(Long stationId, Long lineId) {
        Station station = findStationById(stationId);
        Line line = lineService.findLineById(lineId);

        boolean alreadyConnected =
                station.getStationLines().stream().anyMatch(sl -> sl.getLine().getId().equals(lineId));

        if (!alreadyConnected) {
            station.addLine(line);
        }
    }

    @Transactional
    public void removeLineFromStation(Long stationId, Long lineId) {
        Station station = findStationById(stationId);
        Line line = lineService.findLineById(lineId);
        station.removeLine(line);
    }

    @Transactional(readOnly = true)
    public StationResponseWithFeature findStationWithFeatures(Long id) {
        Station station = findStationById(id);
        Facility facility = station.getFacility();
        List<FloorResponse> floorResponse = floorStrategy.findAllByFacility(facility);

        List<Long> lineIds =
                station.getStationLines().stream()
                        .map(stationLine -> stationLine.getLine().getId())
                        .collect(Collectors.toList());

        FacilityResponseWithFeature facilityResponse =
                FacilityResponseWithFeature.from(
                        facility,
                        fileService.getFileResponse(facility.getDrawingFileId()),
                        fileService.getFileResponse(facility.getThumbnailFileId()));

        List<String> label3DFeatureIds =
                label3DRepository.findAllByFacilityId(facility.getId().toString()).stream() // Use facility ID
                        .map(label3D -> label3D.getFeature().getId())
                        .collect(Collectors.toList());

        List<FeatureResponseWithoutAsset> features =
                FacilityResponseWithFeature.getFeatureResponsesExcludingLabel3D(facility, label3DFeatureIds);

        return StationResponseWithFeature.builder()
                .facility(facilityResponse)
                .floors(floorResponse)
                .lineIds(lineIds)
                .features(features)
                .route(station.getRoute())
                // .subway(station.getSubway()) // subway was in StationResponse, not StationResponseWithFeature in original. Add if needed.
                .build();
    }
}
