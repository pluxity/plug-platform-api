package com.pluxity.facility.facility;

import com.pluxity.facility.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.facility.dto.FacilityHistoryResponse;
import com.pluxity.facility.facility.dto.FacilityResponse;
import com.pluxity.facility.facility.dto.FacilityUpdateRequest;
import com.pluxity.facility.facility.dto.details.FacilityDetailsDto;
import com.pluxity.facility.facility.handler.FacilityHandlerFactory;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final FileService fileService;
    private final EntityManager entityManager;
    private final FacilityRevisionRepository facilityRevisionRepository;
    private final FacilityHandlerFactory handlerFactory;

    private final String PREFIX = "facilities/";

    @Transactional
    public Long save(@Valid FacilityCreateRequest request) {
        checkDuplicateCode(request.code());

        Facility facility = new Facility(
            request.facilityType(),
            request.name(),
            request.code(),
            request.description(),
            null // history comment
        );

        Facility savedFacility = facilityRepository.save(facility);

        String filePath = PREFIX + savedFacility.getId() + "/";
        if (request.drawingFileId() != null) {
            savedFacility.updateDrawingFileId(fileService.finalizeUpload(request.drawingFileId(), filePath));
        }

        if (request.thumbnailFileId() != null) {
            savedFacility.updateThumbnailFileId(fileService.finalizeUpload(request.thumbnailFileId(), filePath));
        }

        if (request.details() != null) {
            saveFacilityDetails(savedFacility, request.facilityType(), request.details());
        }

        return savedFacility.getId();
    }

    @Transactional
    public void update(Long id, @Valid FacilityUpdateRequest request) {
        Facility facility = findFacilityById(id);

        if (request.code() != null && !request.code().equals(facility.getCode())) {
            checkDuplicateCode(request.code());
            facility.updateCode(request.code());
        }
        if (request.name() != null) {
            facility.updateName(request.name());
        }
        if (request.description() != null) {
            facility.updateDescription(request.description());
        }

        if (request.thumbnailFileId() != null
                && !request.thumbnailFileId().equals(facility.getThumbnailFileId())) {
            String filePath = PREFIX + facility.getId() + "/";
            facility.updateThumbnailFileId(
                    fileService.finalizeUpload(request.thumbnailFileId(), filePath));
        }

        if (request.drawingFileId() != null
                && !request.drawingFileId().equals(facility.getDrawingFileId())) {
            String filePath = PREFIX + facility.getId() + "/";
            facility.updateDrawingFileId(fileService.finalizeUpload(request.drawingFileId(), filePath));
        }

        if (request.details() != null) {
            updateFacilityDetails(facility, request.details());
        }
    }

    @Transactional(readOnly = true)
    public FacilityResponse findById(Long id) {
        Facility facility = findFacilityById(id);
        FacilityDetailsDto details = getFacilityDetails(facility);

        return FacilityResponse.from(
            facility,
            fileService.getFileResponse(facility.getDrawingFileId()),
            fileService.getFileResponse(facility.getThumbnailFileId()),
            details
        );
    }

    @Transactional
    public void delete(Long id) {
        Facility facility = findFacilityById(id);
        deleteFacilityDetails(facility);
        facilityRepository.delete(facility);
    }

    private void saveFacilityDetails(Facility facility, FacilityType type, FacilityDetailsDto details) {
        if (details == null) {
            log.warn("No details to save for facility type: {}", type);
            return;
        }
        handlerFactory.getHandler(type).saveDetails(facility, details);
    }

    private void updateFacilityDetails(Facility facility, FacilityDetailsDto details) {
        if (details == null) {
            log.warn("No details to update for facility type: {}", facility.getType());
            return;
        }
        handlerFactory.getHandler(facility.getType()).updateDetails(facility, details);
    }

    private FacilityDetailsDto getFacilityDetails(Facility facility) {
        return handlerFactory.getHandler(facility.getType()).getDetails(facility);
    }

    private void deleteFacilityDetails(Facility facility) {
        handlerFactory.getHandler(facility.getType()).deleteDetails(facility);
    }

    public Facility findFacilityById(Long id) {
        return facilityRepository.findById(id).orElseThrow(() -> new CustomException(
            "Facility not found", HttpStatus.NOT_FOUND, "해당 시설을 찾을 수 없습니다."));
    }

    private void checkDuplicateCode(String code) {
        facilityRepository.findByCode(code).ifPresent(facility -> {
            throw new CustomException("Duplicate code", HttpStatus.BAD_REQUEST, String.format("이미 존재하는 코드입니다: %s", code));
        });
    }

    @Transactional(readOnly = true)
    public Facility findByCode(String code) {
        return facilityRepository
                .findByCode(code)
                .orElseThrow(
                        () ->
                                new CustomException(
                                        "Facility not found",
                                        HttpStatus.NOT_FOUND,
                                        String.format("코드 %s인 시설을 찾을 수 없습니다", code)));
    }

    @Transactional(readOnly = true)
    protected List<Facility> findAll() {
        return facilityRepository.findAll();
    }

    @Transactional(readOnly = true)
    protected List<Facility> findByType(String facilityType) {
        return null;
    }

    @Transactional(readOnly = true)
    public List<FacilityHistoryResponse> findFacilityHistories(Long facilityId) {
        facilityRepository
                .findById(facilityId)
                .orElseThrow(
                        () ->
                                new CustomException(
                                        "Facility not found", HttpStatus.NOT_FOUND, "해당 시설을 찾을 수 없습니다."));

        try {
            List<FacilityHistoryResponse> historyResponses = new ArrayList<>();
            facilityRevisionRepository
                    .findRevisions(facilityId)
                    .forEach(
                            revision -> {
                                Facility facility = revision.getEntity();
                                Date revisionDate =
                                        revision
                                                .getMetadata()
                                                .getRevisionInstant()
                                                .map(instant -> new Date(instant.toEpochMilli()))
                                                .orElse(new Date());
                                String revisionType = revision.getMetadata().getRevisionType().name();
                                
                                historyResponses.add(
                                        new FacilityHistoryResponse(
                                                revision.getMetadata().getRevisionNumber().orElse(null),
                                                revisionDate,
                                                revisionType,
                                                facility.getId(),
                                                facility.getType(),
                                                facility.getCode(),
                                                facility.getName(),
                                                facility.getDescription(),
                                                getDrawingFileResponse(facility),
                                                getThumbnailFileResponse(facility)
                                            ));
                            });

            return historyResponses;
        } catch (Exception e) {
            log.error("Failed to fetch facility history: {}", e.getMessage());
            throw new CustomException(
                    "Failed to fetch facility history", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public FileResponse getDrawingFileResponse(Facility facility) {
        if (facility.getDrawingFileId() == null) {
            return FileResponse.empty();
        }
        try {
            return fileService.getFileResponse(facility.getDrawingFileId());
        } catch (Exception e) {
            log.error("Failed to get drawing file: {}", e.getMessage());
            return FileResponse.empty();
        }
    }

    public FileResponse getThumbnailFileResponse(Facility facility) {
        if (facility.getThumbnailFileId() == null) {
            return FileResponse.empty();
        }
        try {
            return fileService.getFileResponse(facility.getThumbnailFileId());
        } catch (Exception e) {
            log.error("Failed to get thumbnail file: {}", e.getMessage());
            return FileResponse.empty();
        }
    }
}
