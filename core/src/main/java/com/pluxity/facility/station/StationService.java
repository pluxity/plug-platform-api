package com.pluxity.facility.station;

import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityService;
import com.pluxity.facility.dto.FacilityHistoryResponse;
import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.dto.FacilityResponseWithFeature;
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

        Station station =
                Station.builder()
                        .name(request.facility().name())
                        .description(request.facility().description())
                        .build();

        Facility saved = facilityService.save(station, request.facility());

        if (request.floors() != null) {
            for (FloorRequest floorRequest : request.floors()) {
                floorStrategy.save(saved, floorRequest);
            }
        }

        if (request.lineIds() != null && !request.lineIds().isEmpty()) {
            for (Long lineId : request.lineIds()) {
                Line line = lineService.findLineById(lineId);
                station.addLine(line);
            }
        }

        return saved.getId();
    }

    @Transactional(readOnly = true)
    public List<StationResponse> findAll() {
        return stationRepository.findAll().stream()
                .map(
                        station -> {
                            List<Long> lineIds =
                                    station.getStationLines().stream()
                                            .map(stationLine -> stationLine.getLine().getId())
                                            .collect(Collectors.toList());

                            List<FloorResponse> floorResponse = floorStrategy.findAllByFacility(station);
                            List<String> featureIds = station.getFeatures().stream().map(Feature::getId).toList();
                            FacilityResponseWithFeature.getFeatureResponses(station);

                            return StationResponse.builder()
                                    .facility(
                                            FacilityResponse.from(
                                                    station,
                                                    fileService.getFileResponse(station.getDrawingFileId()),
                                                    fileService.getFileResponse(station.getThumbnailFileId())))
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
        Station station = (Station) facilityService.findById(id);
        List<FloorResponse> floorResponse = floorStrategy.findAllByFacility(station);
        List<String> featureIds = station.getFeatures().stream().map(Feature::getId).toList();

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
        return facilityService.findFacilityHistories(id);
    }

    @Transactional
    public void update(Long id, StationUpdateRequest request) {
        // 먼저 스테이션을 조회
        Station station = findStationById(id);

        facilityService.update(id, request.facility());

        // 추가 정보 업데이트
        if (request.floors() != null) {
            floorStrategy.delete(station);
            for (FloorRequest floorRequest : request.floors()) {
                floorStrategy.save(station, floorRequest);
            }
        }

        if (request.lineIds() != null) {
            station.getStationLines().clear();
            for (Long lineId : request.lineIds()) {
                Line line = lineService.findLineById(lineId);
                station.addLine(line);
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        // 삭제할 스테이션 조회
        Station station = findStationById(id);

        // Floor 삭제 및 Facility 삭제
        floorStrategy.delete(station);
        facilityService.deleteFacility(id);
    }

    @Transactional
    public void addLineToStation(Long stationId, Long lineId) {
        Station station = findStationById(stationId);
        Line line = lineService.findLineById(lineId);

        // 이미 연결되어 있는지 확인
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
        List<FloorResponse> floorResponse = floorStrategy.findAllByFacility(station);

        List<Long> lineIds =
                station.getStationLines().stream()
                        .map(stationLine -> stationLine.getLine().getId())
                        .collect(Collectors.toList());

        FacilityResponseWithFeature facilityResponse =
                FacilityResponseWithFeature.from(
                        station,
                        fileService.getFileResponse(station.getDrawingFileId()),
                        fileService.getFileResponse(station.getThumbnailFileId()));

        List<String> label3DFeatureIds =
                label3DRepository.findAllByFacilityId(id.toString()).stream()
                        .map(label3D -> label3D.getFeature().getId())
                        .collect(Collectors.toList());

        List<FeatureResponseWithoutAsset> features =
                FacilityResponseWithFeature.getFeatureResponsesExcludingLabel3D(station, label3DFeatureIds);

        return StationResponseWithFeature.builder()
                .facility(facilityResponse)
                .floors(floorResponse)
                .lineIds(lineIds)
                .features(features)
                .route(station.getRoute())
                .build();
    }
}
