package com.pluxity.domains.station;

import com.pluxity.domains.station.dto.BusanSubwayStationResponse;
import com.pluxity.domains.station.dto.SasangStationCreateRequest;
import com.pluxity.domains.station.dto.SasangStationResponse;
import com.pluxity.domains.station.dto.SasangStationUpdateRequest;
import com.pluxity.domains.station.enums.BusanSubwayStation;
import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityService;
import com.pluxity.facility.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.facility.dto.FacilityHistoryResponse;
import com.pluxity.facility.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorRequest;
import com.pluxity.facility.floor.dto.FloorResponse;
import com.pluxity.facility.line.Line;
import com.pluxity.facility.line.LineService;
import com.pluxity.facility.station.StationService;
import com.pluxity.facility.station.dto.StationResponse;
import com.pluxity.facility.station.dto.StationResponseWithFeature;
import com.pluxity.facility.station.dto.StationUpdateRequest;
import com.pluxity.facility.strategy.FloorStrategy;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
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

    @Transactional
    public Long save(SasangStationCreateRequest request) {
        SasangStation sasangStation =
                SasangStation.sasangStationBuilder()
                        .name(request.facility().name())
                        .description(request.facility().description())
                        .route(request.route())
                        .externalCode(request.externalCode())
                        .build();

        // 파일 ID 설정
        FacilityCreateRequest facilityRequest = request.facility();
        sasangStation.updateDrawingFileId(facilityRequest.drawingFileId());
        sasangStation.updateThumbnailFileId(facilityRequest.thumbnailFileId());

        // 저장
        Facility saved = facilityService.save(sasangStation, facilityRequest);

        // 층 정보 저장
        if (request.floors() != null) {
            for (FloorRequest floorRequest : request.floors()) {
                floorStrategy.save(saved, floorRequest);
            }
        }

        // 노선 정보 저장
        if (request.lineIds() != null && !request.lineIds().isEmpty()) {
            for (Long lineId : request.lineIds()) {
                Line line = lineService.findLineById(lineId);
                sasangStation.addLine(line);
            }
        }

        return saved.getId();
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
        SasangStation sasangStation = findSasangStationById(id);

        // 기본 Station 정보 업데이트
        if (request != null) {
            stationService.update(
                    id,
                    StationUpdateRequest.of(
                            request.name(),
                            request.description(),
                            request.thumbnailFileId(),
                            request.lineIds(),
                            request.route()));
        }

        // SasangStation 고유 필드 업데이트
        if (request.externalCode() != null) {
            sasangStation.updateExternalCode(request.externalCode());
        }
    }

    @Transactional
    public void delete(Long id) {
        findSasangStationById(id);
        stationService.delete(id);
    }

    @Transactional
    public void addLineToStation(Long stationId, Long lineId) {
        findSasangStationById(stationId);
        stationService.addLineToStation(stationId, lineId);
    }

    @Transactional
    public void removeLineFromStation(Long stationId, Long lineId) {
        findSasangStationById(stationId);
        stationService.removeLineFromStation(stationId, lineId);
    }

    @Transactional(readOnly = true)
    public List<FacilityHistoryResponse> findFacilityHistories(Long id) {
        findSasangStationById(id);
        return stationService.findFacilityHistories(id);
    }

    @Transactional(readOnly = true)
    public StationResponseWithFeature findStationWithFeatures(Long id) {
        SasangStation sasangStation = findSasangStationById(id);
        StationResponseWithFeature stationResponse = stationService.findStationWithFeatures(id);

        return getStationResponseWithFeature(sasangStation, stationResponse);
    }

    @Transactional(readOnly = true)
    public StationResponseWithFeature findByCode(String code) {
        SasangStation sasangStation = findSasangStationByCode(code);
        StationResponseWithFeature response =
                stationService.findStationWithFeatures(sasangStation.getId());
        return getStationResponseWithFeature(sasangStation, response);
    }

    private StationResponseWithFeature getStationResponseWithFeature(
            SasangStation sasangStation, StationResponseWithFeature stationResponse) {
        StationResponseWithFeature.AdjacentStationInfo precedingStation =
                findPrecedingStationInfo(sasangStation);
        StationResponseWithFeature.AdjacentStationInfo followingStation =
                findFollowingStationInfo(sasangStation);

        return StationResponseWithFeature.builder()
                .facility(stationResponse.facility())
                .floors(stationResponse.floors())
                .lineIds(stationResponse.lineIds())
                .features(stationResponse.features())
                .route(stationResponse.route())
                .externalCode(sasangStation.getExternalCode())
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
        return sasangStationRepository
                .findByCode(code)
                .orElseThrow(
                        () ->
                                new CustomException(
                                        "SasangStation not found", HttpStatus.NOT_FOUND, "해당 코드의 역을 찾을 수 없습니다."));
    }

    private SasangStationResponse convertToResponse(SasangStation sasangStation) {
        StationResponse stationResponse = convertToStationResponse(sasangStation);
        return SasangStationResponse.of(stationResponse, sasangStation.getExternalCode());
    }

    private StationResponseWithFeature.AdjacentStationInfo findPrecedingStationInfo(
            SasangStation sasangStation) {
        if (sasangStation.getCode() == null) {
            return null;
        }

        Optional<BusanSubwayStation> currentStation =
                BusanSubwayStation.findByCode(sasangStation.getCode());
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

    private StationResponseWithFeature.AdjacentStationInfo findFollowingStationInfo(
            SasangStation sasangStation) {
        if (sasangStation.getCode() == null) {
            return null;
        }

        Optional<BusanSubwayStation> currentStation =
                BusanSubwayStation.findByCode(sasangStation.getCode());
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

    private StationResponse convertToStationResponse(SasangStation station) {
        List<FloorResponse> floorResponse = floorStrategy.findAllByFacility(station);

        List<Long> lineIds =
                station.getStationLines().stream()
                        .map(stationLine -> stationLine.getLine().getId())
                        .collect(Collectors.toList());

        return StationResponse.builder()
                .facility(
                        FacilityResponse.from(
                                station,
                                fileService.getFileResponse(station.getDrawingFileId()),
                                fileService.getFileResponse(station.getThumbnailFileId())))
                .floors(floorResponse)
                .lineIds(lineIds)
                .route(station.getRoute())
                .build();
    }
}
