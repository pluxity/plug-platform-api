package com.pluxity.facility.station;

import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityResponse;
import com.pluxity.facility.facility.FacilityService;
import com.pluxity.facility.facility.dto.FacilityHistoryResponse;
import com.pluxity.facility.floor.dto.FloorRequest;
import com.pluxity.facility.floor.dto.FloorResponse;
import com.pluxity.facility.line.Line;
import com.pluxity.facility.line.LineService;
import com.pluxity.facility.station.dto.StationCreateRequest;
import com.pluxity.facility.station.dto.StationResponse;
import com.pluxity.facility.station.dto.StationUpdateRequest;
import com.pluxity.facility.strategy.FloorStrategy;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import java.util.List;
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

    @Transactional
    public Long save(StationCreateRequest request) {

        Station station =
                Station.builder()
                        .name(request.facility().name())
                        .description(request.facility().description())
                        .route(request.route())
                        .build();

        Facility saved = facilityService.save(station, request.facility());

        if (request.floors() != null) {
            for (FloorRequest floorRequest : request.floors()) {
                floorStrategy.save(saved, floorRequest);
            }
        }

        if (request.lineId() != null) {
            Line line = lineService.findLineById(request.lineId());
            station.changeLine(line);
        }

        return saved.getId();
    }

    @Transactional(readOnly = true)
    public List<StationResponse> findAll() {
        return stationRepository.findAll().stream()
                .map(
                        station ->
                                StationResponse.builder()
                                        .facility(
                                                FacilityResponse.from(
                                                        station,
                                                        fileService.getFileResponse(station.getDrawingFileId()),
                                                        fileService.getFileResponse(station.getThumbnailFileId())))
                                        .lineId(station.getLine() != null ? station.getLine().getId() : null)
                                        .route(station.getRoute())
                                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public StationResponse findById(Long id) {
        Station station = (Station) facilityService.findById(id);
        List<FloorResponse> floorResponse = floorStrategy.findAllByFacility(station);

        return StationResponse.builder()
                .facility(
                        FacilityResponse.from(
                                station,
                                fileService.getFileResponse(station.getDrawingFileId()),
                                fileService.getFileResponse(station.getThumbnailFileId())))
                .floors(floorResponse)
                .lineId(station.getLine() != null ? station.getLine().getId() : null)
                .route(station.getRoute())
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

        // 기본 정보 업데이트
        facilityService.update(
                id, Station.builder().name(request.name()).description(request.description()).build());

        // 추가 정보 업데이트
        if (request.route() != null) {
            station.updateRoute(request.route());
        }

        // drawingFileId와 thumbnailFileId 직접 설정
        if (request.drawingFileId() != null) {
            station.updateDrawingFileId(request.drawingFileId());
        }

        if (request.thumbnailFileId() != null) {
            station.updateThumbnailFileId(request.thumbnailFileId());
        }

        if (request.lineId() != null) {
            Line line = lineService.findLineById(request.lineId());
            station.changeLine(line);
        }
    }

    @Transactional
    public void delete(Long id) {
        // 삭제할 스테이션 조회
        Station station = findStationById(id);

        // Line과의 관계 제거
        station.changeLine(null);

        // Floor 삭제 및 Facility 삭제
        floorStrategy.delete(station);
        facilityService.deleteFacility(id);
    }
}
