package com.pluxity.domains.station;

import com.pluxity.domains.station.dto.SasangStationCreateRequest;
import com.pluxity.domains.station.dto.SasangStationResponse;
import com.pluxity.domains.station.dto.SasangStationUpdateRequest;
import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityResponse;
import com.pluxity.facility.facility.FacilityService;
import com.pluxity.facility.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.facility.dto.FacilityHistoryResponse;
import com.pluxity.facility.floor.dto.FloorRequest;
import com.pluxity.facility.floor.dto.FloorResponse;
import com.pluxity.facility.line.Line;
import com.pluxity.facility.line.LineService;
import com.pluxity.facility.station.StationService;
import com.pluxity.facility.station.dto.StationResponse;
import com.pluxity.facility.strategy.FloorStrategy;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import java.util.List;
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
                        .name(request.stationRequest().facility().name())
                        .description(request.stationRequest().facility().description())
                        .route(request.stationRequest().route())
                        .code(request.code())
                        .externalCode(request.externalCode())
                        .build();

        // 파일 ID 설정
        FacilityCreateRequest facilityRequest = request.stationRequest().facility();
        sasangStation.updateDrawingFileId(facilityRequest.drawingFileId());
        sasangStation.updateThumbnailFileId(facilityRequest.thumbnailFileId());

        // 저장
        Facility saved = facilityService.save(sasangStation, facilityRequest);

        // 층 정보 저장
        if (request.stationRequest().floors() != null) {
            for (FloorRequest floorRequest : request.stationRequest().floors()) {
                floorStrategy.save(saved, floorRequest);
            }
        }

        // 노선 정보 저장
        if (request.stationRequest().lineId() != null) {
            Line line = lineService.findLineById(request.stationRequest().lineId());
            sasangStation.addLine(line);
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
    public SasangStationResponse findByCode(String code) {
        SasangStation sasangStation =
                sasangStationRepository
                        .findByCode(code)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "SasangStation not found", HttpStatus.NOT_FOUND, "해당 코드의 역을 찾을 수 없습니다."));

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
        if (request.stationUpdateRequest() != null) {
            stationService.update(id, request.stationUpdateRequest());
        }

        // SasangStation 고유 필드 업데이트
        if (request.code() != null) {
            sasangStation.updateCode(request.code());
        }

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

    private SasangStation findSasangStationById(Long id) {
        return sasangStationRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new CustomException(
                                        "SasangStation not found", HttpStatus.NOT_FOUND, "해당 ID의 역을 찾을 수 없습니다."));
    }

    private SasangStationResponse convertToResponse(SasangStation sasangStation) {
        StationResponse stationResponse = convertToStationResponse(sasangStation);

        return SasangStationResponse.builder()
                .stationResponse(stationResponse)
                .code(sasangStation.getCode())
                .externalCode(sasangStation.getExternalCode())
                .build();
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
