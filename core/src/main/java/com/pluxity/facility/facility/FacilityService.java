package com.pluxity.facility.facility;

import com.pluxity.facility.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.facility.dto.FacilityHistoryResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import jakarta.persistence.EntityManager;
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

    private final String PREFIX = "facilities/";
    private final FacilityRevisionRepository facilityRevisionRepository;

    @Transactional
    public Facility save(Facility facility, FacilityCreateRequest request) {
        try {
            Facility savedFacility = facilityRepository.save(facility);

            String filePath = PREFIX + savedFacility.getId() + "/";
            if (request.drawingFileId() != null) {
                facility.updateDrawingFileId(fileService.finalizeUpload(request.drawingFileId(), filePath));
            }

            if (request.thumbnailFileId() != null) {
                facility.updateThumbnailFileId(
                        fileService.finalizeUpload(request.thumbnailFileId(), filePath));
            }

            return savedFacility;
        } catch (Exception e) {
            log.error("Facility creation failed: {}", e.getMessage());
            throw new CustomException(
                    "Facility creation failed", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Facility findById(Long id) {
        return facilityRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new CustomException(
                                        "Facility not found", HttpStatus.NOT_FOUND, "해당 시설을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    protected List<Facility> findAll() {
        return facilityRepository.findAll();
    }

    @Transactional(readOnly = true)
    protected List<Facility> findByType(String facilityType) {
        return null;
    }

    @Transactional
    public void update(Long id, Facility newFacility) {
        Facility facility =
                facilityRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Facility not found", HttpStatus.NOT_FOUND, "해당 시설을 찾을 수 없습니다."));
        facility.update(newFacility);
        facilityRepository.save(facility);
    }

    @Transactional
    public void deleteFacility(Long id) {
        Facility facility =
                facilityRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                "Facility not found", HttpStatus.NOT_FOUND, "해당 시설을 찾을 수 없습니다."));

        facilityRepository.delete(facility);
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
            // Spring Data Envers를 이용한 이력 조회
            List<FacilityHistoryResponse> historyResponses = new ArrayList<>();
            facilityRevisionRepository
                    .findRevisions(facilityId)
                    .forEach(
                            revision -> {
                                Facility facility = revision.getEntity();
                                // getMetadata()로 RevisionMetadata 객체 접근
                                Date revisionDate =
                                        revision
                                                .getMetadata()
                                                .getRevisionInstant()
                                                .map(instant -> new Date(instant.toEpochMilli()))
                                                .orElse(new Date());

                                String revisionType = revision.getMetadata().getRevisionType().name();

                                historyResponses.add(
                                        new FacilityHistoryResponse(
                                                facilityId,
                                                facility.getClass().getSimpleName(),
                                                facility.getName(),
                                                facility.getDescription(),
                                                facility.getDrawingFileId(),
                                                facility.getThumbnailFileId(),
                                                revisionDate,
                                                revisionType));
                            });

            return historyResponses;
        } catch (Exception e) {
            log.error("Failed to fetch facility history: {}", e.getMessage());
            throw new CustomException(
                    "Failed to fetch facility history", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
