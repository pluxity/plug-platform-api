package com.pluxity.station;

import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityService;
import com.pluxity.facility.dto.FacilityHistoryResponse;
import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse;
import com.pluxity.facility.strategy.FloorService;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.entity.Feature;
import com.pluxity.file.service.FileService;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.FacilityMappingUtil;
import com.pluxity.label3d.Label3DRepository;
import com.pluxity.label3d.Label3DResponse;
import com.pluxity.station.dto.StationCreateRequest;
import com.pluxity.station.dto.StationResponse;
import com.pluxity.station.dto.StationResponseWithFeature;
import com.pluxity.station.dto.StationUpdateRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StationService {

    private final FileService fileService;
    private final FacilityService facilityService;
    private final FloorService floorService;
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

        floorService.save(saved, request.floors());

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

                            List<FloorResponse> floorResponse = floorService.findAllByFacility(station);
                            List<String> featureIds = station.getFeatures().stream().map(Feature::getId).toList();

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
        List<FloorResponse> floorResponse = floorService.findAllByFacility(station);
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
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STATION, id));
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
        floorService.save(station, request.floors());

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
        floorService.delete(station);
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
        List<FloorResponse> floorResponse = floorService.findAllByFacility(station);

        List<Long> lineIds =
                station.getStationLines().stream()
                        .map(stationLine -> stationLine.getLine().getId())
                        .collect(Collectors.toList());

        FacilityResponse facilityResponse =
                FacilityResponse.from(
                        station,
                        fileService.getFileResponse(station.getDrawingFileId()),
                        fileService.getFileResponse(station.getThumbnailFileId()));

        List<String> label3DFeatureIds =
                label3DRepository.findAllByFacilityId(id.toString()).stream()
                        .map(label3D -> label3D.getFeature().getId())
                        .toList();

        List<FeatureResponse> features =
                station.getFeatures().stream()
                        .filter(feature -> !label3DFeatureIds.contains(feature.getId()))
                        .map(FeatureResponse::from)
                        .collect(Collectors.toList());

        List<Label3DResponse> label3Ds =
                label3DRepository.findAllByFacilityId(id.toString()).stream()
                        .map(Label3DResponse::from)
                        .collect(Collectors.toList());

        return StationResponseWithFeature.builder()
                .facility(facilityResponse)
                .floors(floorResponse)
                .lineIds(lineIds)
                .features(features)
                .label3Ds(label3Ds)
                .route(station.getRoute())
                .subway(station.getSubway())
                .build();
    }

    @Transactional(readOnly = true)
    public List<FacilityResponse> findAllFacilities() {
        List<Station> stations = stationRepository.findAll();
        return FacilityMappingUtil.mapWithFiles(stations, fileService);
    }
}
