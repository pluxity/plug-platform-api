package com.pluxity.domains.sasang.station; // Updated package

import com.pluxity.domains.sasang.station.SasangStation;
import com.pluxity.domains.sasang.station.SasangStationRepository;
import com.pluxity.domains.sasang.station.dto.BusanSubwayStationResponse; // Updated DTO path
import com.pluxity.domains.sasang.station.dto.SasangStationCreateRequest; // Updated DTO path
import com.pluxity.domains.sasang.station.dto.SasangStationResponse; // Updated DTO path
import com.pluxity.domains.sasang.station.dto.SasangStationUpdateRequest; // Updated DTO path
import com.pluxity.domains.sasang.station.enums.BusanSubwayStation; // Updated enum path
import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityService;
import com.pluxity.facility.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.facility.dto.FacilityHistoryResponse;
import com.pluxity.facility.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorRequest;
import com.pluxity.facility.floor.dto.FloorResponse;
import com.pluxity.facility.line.Line;
import com.pluxity.facility.line.LineService;
// import com.pluxity.facility.station.Station; // Not directly used as type, but through SasangStation.getStation()
import com.pluxity.facility.station.StationService;
import com.pluxity.facility.station.dto.StationResponse;
import com.pluxity.facility.station.dto.StationResponseWithFeature;
import com.pluxity.facility.station.dto.StationUpdateRequest;
import com.pluxity.facility.strategy.FloorStrategy;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import com.pluxity.label3d.Label3DResponse;
import com.pluxity.label3d.Label3DService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SasangStationService {

    private final SasangStationRepository sasangStationRepository;
    private final StationService stationService;
    private final FacilityService facilityService;
    private final FileService fileService;
    private final FloorStrategy floorStrategy;
    private final LineService lineService;
    private final Label3DService label3DService;

    @Transactional
    public Long save(SasangStationCreateRequest request) {
        createValidation(request); // Assuming this validation logic will be adapted or is okay for now

        // SasangStation constructor now handles creating the composed Station object,
        // which in turn creates its Facility object.
        SasangStation sasangStation = new SasangStation(
                request.facility().name(),
                request.facility().description(),
                request.route(), // Assuming SasangStationCreateRequest has getRoute()
                request.externalCode()
        );

        FacilityCreateRequest facilityRequest = request.facility();
        // Update drawing and thumbnail file IDs on the composed Facility
        if (facilityRequest.drawingFileId() != null) {
            sasangStation.getStation().getFacility().updateDrawingFileId(facilityRequest.drawingFileId());
        }
        if (facilityRequest.thumbnailFileId() != null) {
            sasangStation.getStation().getFacility().updateThumbnailFileId(facilityRequest.thumbnailFileId());
        }

        // Save the composed Facility details via FacilityService
        // FacilityService.save might update the facility object passed to it.
        facilityService.save(sasangStation.getStation().getFacility(), facilityRequest);

        // Save SasangStation entity
        sasangStationRepository.save(sasangStation);


        // Floor information processing using the composed facility
        if (request.floors() != null) {
            for (FloorRequest floorRequest : request.floors()) {
                floorStrategy.save(sasangStation.getStation().getFacility(), floorRequest);
            }
        }

        // Line information processing using the composed station
        if (request.lineIds() != null && !request.lineIds().isEmpty()) {
            for (Long lineId : request.lineIds()) {
                Line line = lineService.findLineById(lineId);
                sasangStation.getStation().addLine(line);
            }
        }

        return sasangStation.getId(); // Return ID of the saved SasangStation
    }

    private void createValidation(SasangStationCreateRequest request) {
        // TODO: Validation logic needs review due to Facility.code and Facility.name now being indirect.
        // Assuming repository methods are adapted or this is handled separately.
        sasangStationRepository
                .findByCode(request.facility().code()) // This needs to search via Facility.code
                .ifPresent(
                        station -> {
                            throw new CustomException(
                                    "Station Code Already Exists", // Swapped Name and Code here to match original logic's string format
                                    HttpStatus.BAD_REQUEST,
                                    String.format("코드가 %s인 역사가 이미 존재합니다", request.facility().code())); // Corrected message to use code
                        });
        sasangStationRepository
                .findByName(request.facility().name()) // This needs to search via Facility.name
                .ifPresent(
                        station -> {
                            throw new CustomException(
                                    "Station Name Already Exists",
                                    HttpStatus.BAD_REQUEST,
                                    String.format("이름이 %s인 역사가 이미 존재합니다", request.facility().name())); // Corrected message to use name
                        });
    }

    @Transactional(readOnly = true)
    public List<SasangStationResponse> findAll() {
        return sasangStationRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SasangStationResponse findById(Long id) {
        SasangStation sasangStation = findSasangStationById(id);
        return convertToResponse(sasangStation);
    }

    @Transactional(readOnly = true)
    public SasangStationResponse findByExternalCode(String externalCode) {
        SasangStation sasangStation =
                sasangStationRepository
                        .findByExternalCode(externalCode)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "SasangStation not found",
                                                HttpStatus.NOT_FOUND,
                                                "해당 외부 코드의 역을 찾을 수 없습니다."));

        return convertToResponse(sasangStation);
    }

    @Transactional
    public void update(Long id, SasangStationUpdateRequest request) {
        updateValidation(id, request); // Assuming validation logic is adapted
        SasangStation sasangStation = findSasangStationById(id);

        // Update composed Station's information via StationService
        // Pass the ID of the composed Station
        stationService.update(
                sasangStation.getStation().getId(),
                new StationUpdateRequest(request.facility(), request.floors(), request.lineIds()));

        // Update SasangStation's specific fields
        if (request.externalCode() != null) {
            sasangStation.updateExternalCode(request.externalCode());
        }
        // sasangStationRepository.save(sasangStation); // If changes to sasangStation itself need saving
    }

    private void updateValidation(Long id, SasangStationUpdateRequest request) {
        // TODO: Validation logic needs review due to Facility.name and Facility.code now being indirect.
        // Assuming repository methods are adapted or this is handled separately.
        sasangStationRepository
                .findByNameAndIdNot(request.facility().name(), id) // This needs to search via Facility.name
                .ifPresent(
                        station -> {
                            throw new CustomException(
                                    "Station Name Already Exists",
                                    HttpStatus.BAD_REQUEST,
                                    String.format("이름이 %s인 역사가 이미 존재합니다", request.facility().name()));
                        });
        sasangStationRepository
                .findByCodeAndIdNot(request.facility().code(), id) // This needs to search via Facility.code
                .ifPresent(
                        station -> {
                            throw new CustomException(
                                    "Station Code Already Exists",
                                    HttpStatus.BAD_REQUEST,
                                    String.format("코드가 %s인 역사가 이미 존재합니다", request.facility().code()));
                        });
    }

    @Transactional
    public void delete(Long id) {
        SasangStation sasangStation = findSasangStationById(id);
        // Deleting SasangStation should cascade to composed Station and Facility
        // due to orphanRemoval=true and CascadeType.ALL settings.
        sasangStationRepository.delete(sasangStation);
    }

    @Transactional
    public void addLineToStation(Long stationId, Long lineId) {
        SasangStation sasangStation = findSasangStationById(stationId);
        // Add line to the composed Station
        stationService.addLineToStation(sasangStation.getStation().getId(), lineId);
    }

    @Transactional
    public void removeLineFromStation(Long stationId, Long lineId) {
        SasangStation sasangStation = findSasangStationById(stationId);
        // Remove line from the composed Station
        stationService.removeLineFromStation(sasangStation.getStation().getId(), lineId);
    }

    @Transactional(readOnly = true)
    public List<FacilityHistoryResponse> findFacilityHistories(Long id) {
        SasangStation sasangStation = findSasangStationById(id);
        // Fetch histories for the Facility composed within the Station of SasangStation
        return stationService.findFacilityHistories(sasangStation.getStation().getId());
    }

    @Transactional(readOnly = true)
    public StationResponseWithFeature findStationWithFeatures(Long id) {
        SasangStation sasangStation = findSasangStationById(id);
        // Fetch features for the composed Station
        StationResponseWithFeature stationResponse =
                stationService.findStationWithFeatures(sasangStation.getStation().getId());

        // Populate SasangStation specific details into the response
        return getStationResponseWithFeature(sasangStation, stationResponse);
    }

    @Transactional(readOnly = true)
    public StationResponseWithFeature findByCode(String code) {
        // TODO: findSasangStationByCode needs to be adapted for Facility.code being indirect.
        SasangStation sasangStation = findSasangStationByCode(code);
        StationResponseWithFeature response =
                stationService.findStationWithFeatures(sasangStation.getStation().getId());
        return getStationResponseWithFeature(sasangStation, response);
    }

    private StationResponseWithFeature getStationResponseWithFeature(
            SasangStation sasangStation, StationResponseWithFeature stationResponse) {

        StationResponseWithFeature.AdjacentStationInfo precedingStation =
                findPrecedingStationInfo(sasangStation);
        StationResponseWithFeature.AdjacentStationInfo followingStation =
                findFollowingStationInfo(sasangStation);

        // Use Facility ID for Label3D services
        List<Label3DResponse> label3Ds = label3DService.getLabel3DsByFacilityId(
                sasangStation.getStation().getFacility().getId().toString());

        return StationResponseWithFeature.builder()
                .facility(stationResponse.facility())
                .floors(stationResponse.floors())
                .lineIds(stationResponse.lineIds())
                .features(stationResponse.features())
                .label3Ds(label3Ds)
                .route(stationResponse.route()) // Route from the composed Station
                .externalCode(sasangStation.getExternalCode()) // SasangStation specific
                .subway(sasangStation.getStation().getSubway()) // Subway from composed Station
                .precedingStation(precedingStation)
                .followingStation(followingStation)
                .build();
    }

    @Transactional(readOnly = true)
    public List<BusanSubwayStationResponse> findAllBusanSubwayStations() {
        return BusanSubwayStation.findAll().stream()
                .map(BusanSubwayStationResponse::from)
                .collect(Collectors.toList());
    }

    private SasangStation findSasangStationById(Long id) {
        return sasangStationRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new CustomException(
                                        "SasangStation not found", HttpStatus.NOT_FOUND, "해당 ID의 역을 찾을 수 없습니다."));
    }

    private SasangStation findSasangStationByCode(String code) {
        // TODO: This repository method needs adaptation for indirect Facility.code
        return sasangStationRepository
                .findByCode(code) // Assumes SasangStationRepository can search by Facility.code
                .orElseThrow(
                        () ->
                                new CustomException(
                                        "SasangStation not found", HttpStatus.NOT_FOUND, "해당 코드의 역을 찾을 수 없습니다."));
    }

    private SasangStationResponse convertToResponse(SasangStation sasangStation) {
        StationResponse stationResponse = convertToStationResponse(sasangStation);
        return SasangStationResponse.of(stationResponse, sasangStation.getExternalCode());
    }

    // This method needs to access composed objects for properties
    private StationResponseWithFeature.AdjacentStationInfo findPrecedingStationInfo(
            SasangStation sasangStation) {
        String facilityCode = sasangStation.getStation().getFacility().getCode();
        if (facilityCode == null) {
            return null;
        }

        Optional<BusanSubwayStation> currentStation = BusanSubwayStation.findByCode(facilityCode);
        if (currentStation.isEmpty()) {
            return null;
        }

        Optional<BusanSubwayStation> precedingStation = currentStation.get().getPrecedingStation();
        return precedingStation
                .map(
                        station ->
                                StationResponseWithFeature.AdjacentStationInfo.of(
                                        station.getCode(), station.getName()))
                .orElse(null);
    }

    // This method needs to access composed objects for properties
    private StationResponseWithFeature.AdjacentStationInfo findFollowingStationInfo(
            SasangStation sasangStation) {
        String facilityCode = sasangStation.getStation().getFacility().getCode();
        if (facilityCode == null) {
            return null;
        }

        Optional<BusanSubwayStation> currentStation = BusanSubwayStation.findByCode(facilityCode);
        if (currentStation.isEmpty()) {
            return null;
        }

        Optional<BusanSubwayStation> followingStation = currentStation.get().getFollowingStation();
        return followingStation
                .map(
                        station ->
                                StationResponseWithFeature.AdjacentStationInfo.of(
                                        station.getCode(), station.getName()))
                .orElse(null);
    }

    // This helper converts SasangStation to StationResponse, accessing composed objects
    private StationResponse convertToStationResponse(SasangStation sasangStation) {
        Facility facility = sasangStation.getStation().getFacility(); // Get composed facility
        com.pluxity.facility.station.Station composedStation = sasangStation.getStation(); // Get composed station

        List<FloorResponse> floorResponse = floorStrategy.findAllByFacility(facility);

        List<Long> lineIds =
                composedStation.getStationLines().stream()
                        .map(stationLine -> stationLine.getLine().getId())
                        .collect(Collectors.toList());

        List<String> featureIds = facility.getFeatures().stream() // Get features from facility
                                          .map(feature -> feature.getId())
                                          .collect(Collectors.toList());


        return StationResponse.builder()
                .facility(
                        FacilityResponse.from(
                                facility, // Pass the actual facility object
                                fileService.getFileResponse(facility.getDrawingFileId()),
                                fileService.getFileResponse(facility.getThumbnailFileId())))
                .floors(floorResponse)
                .lineIds(lineIds)
                .featureIds(featureIds) // Added featureIds
                .route(composedStation.getRoute())
                .subway(composedStation.getSubway())
                .build();
    }
}
