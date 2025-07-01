package com.pluxity.domains.station;

import com.pluxity.domains.station.dto.SasangStationCreateRequest;
import com.pluxity.domains.station.dto.SasangStationResponse;
import com.pluxity.domains.station.dto.SasangStationUpdateRequest;
import com.pluxity.domains.station.repository.SasangStationDetailsRepository;
import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityRepository;
import com.pluxity.facility.facility.FacilityService;
import com.pluxity.facility.facility.FacilityType;
import com.pluxity.facility.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.facility.dto.FacilityResponse;
import com.pluxity.facility.facility.repository.StationInfoRepository;
import com.pluxity.global.exception.CustomException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SasangStationService {

    private final FacilityRepository facilityRepository;
    private final FacilityService facilityService;
    private final StationInfoRepository stationInfoRepository;
    private final SasangStationDetailsRepository sasangStationDetailsRepository;

    @Transactional
    public Long save(SasangStationCreateRequest request) {
        FacilityCreateRequest facilityInfo = request.getFacility();

        FacilityCreateRequest facilityCreateRequest = FacilityCreateRequest.builder()
            .facilityType(FacilityType.STATION)
            .name(facilityInfo.name())
            .code(facilityInfo.code())
            .description(facilityInfo.description())
            .drawingFileId(facilityInfo.drawingFileId())
            .thumbnailFileId(facilityInfo.thumbnailFileId())
            .details(Map.of("lineName", "사상선"))
            .build();

        Long facilityId = facilityService.save(facilityCreateRequest);
        Facility facility = facilityRepository.findById(facilityId)
            .orElseThrow(() -> new CustomException("Facility not found", HttpStatus.NOT_FOUND, "저장된 시설을 찾을 수 없습니다."));

        SasangStationDetails details = SasangStationDetails.builder()
            .facility(facility)
            .externalCode(request.getExternalCode())
            .route(request.getRoute())
            .subway(request.getSubway())
            .platformCount(request.getPlatformCount())
            .isTransferStation(request.getIsTransferStation())
            .build();
        sasangStationDetailsRepository.save(details);

        return facilityId;
    }

    @Transactional(readOnly = true)
    public List<SasangStationResponse> findAll() {
        return sasangStationDetailsRepository.findAll().stream()
            .map(details -> {
                FacilityResponse facilityResponse = facilityService.findById(details.getId());
                return SasangStationResponse.from(facilityResponse, details);
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SasangStationResponse findById(Long id) {
        SasangStationDetails details = findDetailsById(id);
        FacilityResponse facilityResponse = facilityService.findById(id);
        return SasangStationResponse.from(facilityResponse, details);
    }

    @Transactional(readOnly = true)
    public SasangStationResponse findByExternalCode(String externalCode) {
        SasangStationDetails details = sasangStationDetailsRepository.findByExternalCode(externalCode)
            .orElseThrow(() -> new CustomException("SasangStation not found", HttpStatus.NOT_FOUND, "해당 외부 코드의 역을 찾을 수 없습니다."));
        FacilityResponse facilityResponse = facilityService.findById(details.getId());
        return SasangStationResponse.from(facilityResponse, details);
    }

    @Transactional
    public void update(Long id, SasangStationUpdateRequest request) {
        if (request.getFacility() != null) {
            facilityService.update(id, request.getFacility());
        }

        SasangStationDetails details = findDetailsById(id);
        if (request.getExternalCode() != null) {
            details.updateExternalCode(request.getExternalCode());
        }
        if (request.getRoute() != null) {
            details.updateRoute(request.getRoute());
        }
        if (request.getSubway() != null) {
            details.updateSubway(request.getSubway());
        }
        if (request.getPlatformCount() != null) {
            details.updatePlatformCount(request.getPlatformCount());
        }
        if (request.getIsTransferStation() != null) {
            details.updateIsTransferStation(request.getIsTransferStation());
        }
    }

    @Transactional
    public void delete(Long id) {
        facilityService.delete(id);
    }

    private SasangStationDetails findDetailsById(Long id) {
        return sasangStationDetailsRepository.findById(id)
            .orElseThrow(() -> new CustomException("SasangStation not found", HttpStatus.NOT_FOUND, "해당 ID의 역을 찾을 수 없습니다."));
    }
}
