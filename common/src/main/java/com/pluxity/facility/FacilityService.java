package com.pluxity.facility;

import static com.pluxity.global.constant.ErrorCode.*;

import com.pluxity.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.dto.FacilityHistoryResponse;
import com.pluxity.facility.dto.FacilityUpdateRequest;
import com.pluxity.facility.history.FacilityHistory;
import com.pluxity.facility.history.FacilityHistoryRepository;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.FacilityMappingUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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

    private final String PREFIX = "facilities/";
    private final FacilityHistoryRepository facilityHistoryRepository;

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
                facilityHistoryRepository.save(
                        FacilityHistory.builder()
                                .fileId(request.drawingFileId())
                                .facilityId(facility.getId())
                                .comment("최초등록")
                                .build());
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

    @Transactional
    public void update(Long id, @Valid FacilityUpdateRequest request) {
        if (request == null) {
            return;
        }
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

        List<FacilityHistory> histories =
                facilityHistoryRepository.findByFacilityIdOrderByCreatedAtDesc(facilityId);
        List<Long> ids = histories.stream().map(FacilityHistory::getFileId).toList();
        Map<Long, FileResponse> fileMap = FacilityMappingUtils.getFileMapByIds(ids, fileService);
        return histories.stream()
                .map(v -> FacilityHistoryResponse.from(v, fileMap.get(v.getFileId())))
                .toList();
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

    @Transactional
    public void updateDrawingFile(Long id, Long drawingFileId, String comment) {
        Facility facility =
                facilityRepository
                        .findById(id)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FACILITY, id));

        String filePath = PREFIX + facility.getId() + "/";
        facility.updateDrawingFileId(fileService.finalizeUpload(drawingFileId, filePath));
        facilityHistoryRepository.save(
                FacilityHistory.builder()
                        .fileId(drawingFileId)
                        .facilityId(facility.getId())
                        .comment(comment)
                        .build());
    }
}
