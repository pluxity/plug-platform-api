package com.pluxity.station;

import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityProvider;
import com.pluxity.facility.FacilityService;
import com.pluxity.facility.dto.FacilityApiType;
import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse;
import com.pluxity.facility.strategy.FloorService;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.MappingUtils;
import com.pluxity.global.utils.SortUtils;
import com.pluxity.label3d.Label3DRepository;
import com.pluxity.label3d.Label3DResponse;
import com.pluxity.station.dto.StationCreateRequest;
import com.pluxity.station.dto.StationResponse;
import com.pluxity.station.dto.StationResponseWithFeature;
import com.pluxity.station.dto.StationUpdateRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StationService implements FacilityProvider {

    private final FileService fileService;
    private final FacilityService facilityService;
    private final FloorService floorService;
    private final StationRepository stationRepository;
    private final LineService lineService;
    private final Label3DRepository label3DRepository;
    private final StationCodeService stationCodeService;
    private final StationLineService stationLineService;

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
                stationLineService.save(station, line);
            }
        }
        if (request.stationCodes() != null && !request.stationCodes().isEmpty()) {
            for (String stationCode : request.stationCodes()) {
                stationCodeService.save(station, stationCode);
            }
        }

        return saved.getId();
    }

    @Transactional(readOnly = true)
    public List<StationResponse> findAll() {
        List<Station> stations = stationRepository.findAll(SortUtils.getOrderByCreatedAtDesc());
        Map<Long, FileResponse> fileMap =
                MappingUtils.getFileMapByIds(
                        stations, v -> Stream.of(v.getDrawingFileId(), v.getThumbnailFileId()), fileService);
        Map<Facility, List<FloorResponse>> floorMap = floorService.findAllByFacilities(stations);
        Map<Station, List<String>> stationCodeMap =
                stationCodeService.findCodeMapByStationIds(stations);
        Map<Station, List<Long>> lineMap = stationLineService.findLineMapByStationIds(stations);
        return stations.stream()
                .map(
                        station ->
                                StationResponse.of(
                                        FacilityResponse.from(
                                                station,
                                                fileMap.get(station.getDrawingFileId()),
                                                fileMap.get(station.getThumbnailFileId())),
                                        floorMap.get(station),
                                        lineMap.get(station),
                                        stationCodeMap.get(station)))
                .toList();
    }

    @Transactional(readOnly = true)
    public StationResponse findById(Long id) {
        Station station = (Station) facilityService.findById(id);
        List<FloorResponse> floorResponse = floorService.findAllByFacility(station);

        return StationResponse.of(
                FacilityResponse.from(
                        station,
                        fileService.getFileResponse(station.getDrawingFileId()),
                        fileService.getFileResponse(station.getThumbnailFileId())),
                floorResponse,
                stationLineService.findLinesByStation(station),
                stationCodeService.findCodesByStation(station));
    }

    @Transactional(readOnly = true)
    public Station findStationById(Long id) {
        return stationRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_STATION, id));
    }

    @Transactional
    public void putUpdate(Long id, StationUpdateRequest request) {
        Station station = findStationById(id);

        facilityService.putUpdate(id, request.facility());
        floorService.update(station, request.floors());

        stationLineService.deleteByStation(station);
        if (request.stationInfo().lineIds() != null) {
            for (Long lineId : request.stationInfo().lineIds()) {
                Line line = lineService.findLineById(lineId);
                stationLineService.save(station, line);
            }
        }

        stationCodeService.deleteByStation(station);
        if (request.stationInfo().stationCodes() != null) {
            for (String code : request.stationInfo().stationCodes()) {
                stationCodeService.save(station, code);
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        // 삭제할 스테이션 조회
        Station station = findStationById(id);

        // Floor 삭제 및 Facility 삭제
        floorService.delete(station);
        stationLineService.deleteByStation(station);
        stationCodeService.deleteByStation(station);
        facilityService.deleteFacility(id);
    }

    @Transactional
    public void addLineToStation(Long stationId, Long lineId) {
        Station station = findStationById(stationId);
        Line line = lineService.findLineById(lineId);

        // 이미 연결되어 있는지 확인
        if (!stationLineService.checkAlreadyConnect(station, line)) {
            stationLineService.save(station, line);
        }
    }

    @Transactional
    public void removeLineFromStation(Long stationId, Long lineId) {
        Station station = findStationById(stationId);
        Line line = lineService.findLineById(lineId);
        stationLineService.deleteStationLine(station, line);
    }

    @Transactional(readOnly = true)
    public StationResponseWithFeature findStationWithFeatures(Long id) {
        Station station = findStationById(id);
        List<FloorResponse> floorResponse = floorService.findAllByFacility(station);

        List<Long> lineIds = stationLineService.findLinesByStation(station);

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

        List<String> stationCodes = stationCodeService.findCodesByStation(station);

        return StationResponseWithFeature.builder()
                .facility(facilityResponse)
                .floors(floorResponse)
                .lineIds(lineIds)
                .features(features)
                .label3Ds(label3Ds)
                .stationCodes(stationCodes)
                .build();
    }

    @Override
    public FacilityApiType getFacilityApiType() {
        return FacilityApiType.STATION;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacilityResponse> getAllFacilities() {
        List<Station> stations = stationRepository.findAll(SortUtils.getOrderByCreatedAtDesc());
        return MappingUtils.mapWithFiles(stations, fileService);
    }
}
