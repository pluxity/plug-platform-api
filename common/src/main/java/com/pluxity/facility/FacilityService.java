package com.pluxity.facility;

import static com.pluxity.global.constant.ErrorCode.*;

import com.pluxity.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.dto.FacilityHistoryResponse;
import com.pluxity.facility.dto.FacilityUpdateRequest;
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
    public Facility save(Facility facility, @Valid FacilityCreateRequest request) {
        try {
            // 코드 중복 검사
            if (request.code() != null && !request.code().isEmpty()) {
                checkDuplicateCode(request.code());
                facility.updateCode(request.code());
            }

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
            throw new IllegalStateException("Facility creation failed", e);
        }
    }

    private void checkDuplicateCode(String code) {
        if (facilityRepository.existsByCode(code)) {
            throw new CustomException(DUPLICATE_FACILITY_CODE, code);
        }
    }

    @Transactional(readOnly = true)
    public Facility findByCode(String code) {
        return facilityRepository
                .findByCode(code)
                .orElseThrow(() -> new CustomException(NOT_FOUND_FACILITY_CODE, code));
    }

    @Transactional(readOnly = true)
    public Facility findById(Long id) {
        return facilityRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(NOT_FOUND_FACILITY, id));
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
    public void update(Long id, @Valid FacilityUpdateRequest request) {
        Facility facility = findById(id);

        // 코드 변경 요청이 있고, 기존 코드와 다른 경우에만 중복 검사
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

        facilityRepository.save(facility);
    }

    @Transactional
    public void update(Long id, Facility newFacility) {
        Facility facility = findById(id);

        // 코드 변경 요청이 있고, 기존 코드와 다른 경우에만 중복 검사
        if (newFacility.getCode() != null && !newFacility.getCode().equals(facility.getCode())) {
            checkDuplicateCode(newFacility.getCode());
        }

        facility.update(newFacility);
        facilityRepository.save(facility);
    }

    @Transactional
    public void deleteFacility(Long id) {
        Facility facility = findById(id);
        facilityRepository.delete(facility);
    }

    @Transactional(readOnly = true)
    public List<FacilityHistoryResponse> findFacilityHistories(Long facilityId) {
        facilityRepository
                .findById(facilityId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_FACILITY, facilityId));

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

                                // 파일 정보 가져오기
                                FileResponse drawingFileResponse = getDrawingFileResponse(facility);
                                FileResponse thumbnailFileResponse = getThumbnailFileResponse(facility);

                                historyResponses.add(
                                        new FacilityHistoryResponse(
                                                facilityId,
                                                facility.getClass().getSimpleName(),
                                                facility.getCode(),
                                                facility.getName(),
                                                facility.getDescription(),
                                                drawingFileResponse,
                                                thumbnailFileResponse,
                                                revisionDate,
                                                revisionType));
                            });

            return historyResponses;
        } catch (Exception e) {
            log.error("Failed to fetch facility history: {}", e.getMessage());
            throw new IllegalStateException("Failed to fetch facility history", e);
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
