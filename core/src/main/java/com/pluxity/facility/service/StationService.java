package com.pluxity.facility.service;

import com.pluxity.facility.dto.*;
import com.pluxity.facility.entity.Facility;
import com.pluxity.facility.entity.Line;
import com.pluxity.facility.entity.Station;
import com.pluxity.facility.repository.LineRepository;
import com.pluxity.facility.repository.StationRepository;
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
    private final LineRepository lineRepository;

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

        if (request.lineId() != null) {
            Line line =
                    lineRepository
                            .findById(request.lineId())
                            .orElseThrow(
                                    () ->
                                            new CustomException(
                                                    "Line not found", HttpStatus.NOT_FOUND, "해당 호선을 찾을 수 없습니다."));
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
                .build();
    }

    @Transactional(readOnly = true)
    public List<FacilityHistoryResponse> findFacilityHistories(Long id) {
        return facilityService.findFacilityHistories(id);
    }

    @Transactional
    public void update(Long id, StationUpdateRequest request) {
        // 먼저 스테이션을 조회
        Station station =
                stationRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Station not found", HttpStatus.NOT_FOUND, "해당 역을 찾을 수 없습니다."));

        // 기본 정보 업데이트
        facilityService.update(
                id, Station.builder().name(request.name()).description(request.description()).build());

        // drawingFileId와 thumbnailFileId 직접 설정
        if (request.drawingFileId() != null) {
            station.updateDrawingFileId(request.drawingFileId());
        }

        if (request.thumbnailFileId() != null) {
            station.updateThumbnailFileId(request.thumbnailFileId());
        }

        if (request.lineId() != null) {
            Line line =
                    lineRepository
                            .findById(request.lineId())
                            .orElseThrow(
                                    () ->
                                            new CustomException(
                                                    "Line not found", HttpStatus.NOT_FOUND, "해당 호선을 찾을 수 없습니다."));
            station.changeLine(line);
        }
    }

    @Transactional
    public void delete(Long id) {
        // 삭제할 스테이션 조회
        Station station =
                stationRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Station not found", HttpStatus.NOT_FOUND, "해당 역을 찾을 수 없습니다."));

        // Line과의 관계 제거
        station.changeLine(null);

        // Floor 삭제 및 Facility 삭제
        floorStrategy.delete(station);
        facilityService.deleteFacility(id);
    }
}
