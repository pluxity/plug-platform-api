package com.pluxity.facility.station;

import com.pluxity.facility.station.mapper.StationMapper; // Added import
import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityService;
import com.pluxity.facility.facility.dto.FacilityHistoryResponse;
import com.pluxity.facility.facility.dto.FacilityResponse; // Still needed for new StationResponse(...)
// import com.pluxity.facility.facility.dto.FacilityResponseWithFeature; // Removed
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
// import com.pluxity.file.service.FileService; // Removed
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

    // private final FileService fileService; // Removed from fields
    private final FacilityService facilityService;
    private final FloorStrategy floorStrategy;
    private final StationRepository stationRepository;
    private final LineService lineService;
    private final Label3DRepository label3DRepository;
    private final StationMapper stationMapper; // Added StationMapper

    // Constructor updated by @RequiredArgsConstructor

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
                .map(station -> {
                    StationResponse stationDto = stationMapper.toStationResponse(station);
                    List<FloorResponse> floorResponses = floorStrategy.findAllByFacility(station.getFacility());
                    // Create new DTO with floors populated
                    return new StationResponse(
                            stationDto.id(),
                            stationDto.facility(),
                            floorResponses, // floors manually set
                            stationDto.lineIds(),
                            stationDto.featureIds(),
                            stationDto.route(),
                            stationDto.subway()
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public StationResponse findById(Long id) {
        Station station = findStationById(id);
        StationResponse stationDto = stationMapper.toStationResponse(station);
        List<FloorResponse> floorResponses = floorStrategy.findAllByFacility(station.getFacility());
        // Create new DTO with floors populated
        return new StationResponse(
                stationDto.id(),
                stationDto.facility(),
                floorResponses, // floors manually set
                stationDto.lineIds(),
                stationDto.featureIds(),
                stationDto.route(),
                stationDto.subway()
        );
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
        // Use mapper for base StationResponse data
        StationResponse baseStationResponse = stationMapper.toStationResponse(station);

        // Get floors separately as it's not mapped by StationMapper
        List<FloorResponse> floorResponses = floorStrategy.findAllByFacility(station.getFacility());

        // Get Label3D related features (this seems specific to StationResponseWithFeature)
        List<String> label3DFeatureIdsFromRepo =
                label3DRepository.findAllByFacilityId(station.getFacility().getId().toString()).stream()
                        .map(label3D -> label3D.getFeature().getId())
                        .collect(Collectors.toList());

        // Get other features, excluding those already covered by Label3D (if this logic is still desired)
        // The original FacilityResponseWithFeature.getFeatureResponsesExcludingLabel3D is complex.
        // For now, let's assume featureIds from baseStationResponse.featureIds() is sufficient,
        // or this part needs a dedicated FeatureMapper or service call.
        // The baseStationResponse.featureIds() already contains all feature IDs from the facility.
        // If specific filtering for Label3D is needed, it has to be applied here.
        // Let's use the featureIds from the base DTO for now.
        List<FeatureResponseWithoutAsset> featuresForResponse = station.getFacility().getFeatures().stream()
            .filter(feature -> !label3DFeatureIdsFromRepo.contains(feature.getId()))
            .map(feature -> new FeatureResponseWithoutAsset(feature.getId(), feature.getName(), feature.getFeatureType())) // Manual mapping
            .collect(Collectors.toList());


        return StationResponseWithFeature.builder()
                .facility(baseStationResponse.facility()) // This is now the simplified FacilityResponse
                .floors(floorResponses) // Manually set floors
                .lineIds(baseStationResponse.lineIds())
                .features(featuresForResponse) // Manually set/filtered features
                .route(baseStationResponse.route())
                // .subway(baseStationResponse.subway()) // Add if StationResponseWithFeature needs subway
                // label3Ds would need to be fetched and set if part of StationResponseWithFeature
                .build();
    }
}
