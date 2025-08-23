package com.pluxity.facility;

import static com.pluxity.global.constant.ErrorCode.*;

import com.pluxity.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.dto.FacilityHistoryResponse;
import com.pluxity.facility.dto.FacilityUpdateRequest;
import com.pluxity.facility.history.FacilityHistoryService;
import com.pluxity.facility.path.FacilityPathService;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.annotation.CheckPermission;
import com.pluxity.global.annotation.CheckPermissionAfter;
import com.pluxity.global.annotation.CheckPermissionAll;
import com.pluxity.global.exception.CustomException;
import jakarta.validation.Valid;
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

    private final String PREFIX = "facilities/";
    private final FacilityHistoryService facilityHistoryService;
    private final FacilityPathService facilityPathService;
    private final FloorService floorService;

    @Transactional
    public Facility save(Facility facility, FacilityCreateRequest request) {
        // 코드 중복 검사
        if (request.code() != null && !request.code().isEmpty()) {
            checkDuplicateCode(request.code());
            facility.updateCode(request.code());
        }

        Facility savedFacility = facilityRepository.save(facility);

        String filePath = PREFIX + savedFacility.getId() + "/";
        if (request.drawingFileId() != null) {
            facility.updateDrawingFileId(fileService.finalizeUpload(request.drawingFileId(), filePath));
            facilityHistoryService.save(request.drawingFileId(), facility.getId(), "최초등록");
        }

        if (request.thumbnailFileId() != null) {
            facility.updateThumbnailFileId(
                    fileService.finalizeUpload(request.thumbnailFileId(), filePath));
        }
        facility.updatePosition(
                FacilityPosition.builder()
                        .lon(request.lon())
                        .lat(request.lat())
                        .locationMeta(request.locationMeta())
                        .build());

        return savedFacility;
    }

    private void checkDuplicateCode(String code) {
        if (facilityRepository.existsByCode(code)) {
            throw new CustomException(DUPLICATE_FACILITY_CODE, code);
        }
    }

    @CheckPermission(type = PermissionType.ID)
    @Transactional(readOnly = true)
    public Facility findByCode(String code) {
        return facilityRepository
                .findByCode(code)
                .orElseThrow(() -> new CustomException(NOT_FOUND_FACILITY_CODE, code));
    }

    @CheckPermission(type = PermissionType.ID)
    @Transactional(readOnly = true)
    public Facility findById(Long id) {
        return facilityRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(NOT_FOUND_FACILITY, id));
    }

    @CheckPermission(type = PermissionType.ID, phase = ExecutionPhase.FILTER)
    @Transactional(readOnly = true)
    public List<Facility> findAll() {
        return facilityRepository.findAll();
    }

    @Transactional
    public void update(Long id, FacilityUpdateRequest request) {
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
        facility.updatePosition(request.lon(), request.lat(), request.locationMeta());
    }

    @Transactional
    public void putUpdate(Long id, FacilityUpdateRequest request) {
        Facility facility = findById(id);

        if (request.code() != null && !request.code().equals(facility.getCode())) {
            checkDuplicateCode(request.code());
        }

        facility.updateCode(request.code());
        facility.updateName(request.name());
        facility.updateDescription(request.description());

        if (request.thumbnailFileId() != null
                && !request.thumbnailFileId().equals(facility.getThumbnailFileId())) {
            String filePath = PREFIX + facility.getId() + "/";
            facility.updateThumbnailFileId(
                    fileService.finalizeUpload(request.thumbnailFileId(), filePath));
        }
        facility.updateThumbnailFileId(request.thumbnailFileId());

        facility.updatePosition(request.lon(), request.lat(), request.locationMeta());
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
        return facilityHistoryService.findByFacilityId(facilityId);
    }

    @Transactional
    public void updateDrawingFile(Long id, FacilityDrawingUpdateRequest request) {
        Facility facility = findById(id);
        String filePath = PREFIX + facility.getId() + "/";
        facility.updateDrawingFileId(fileService.finalizeUpload(request.drawingFileId(), filePath));
        facilityHistoryService.save(request.drawingFileId(), facility.getId(), request.comment());
    }

    @Transactional
    public void savePath(Long facilityId, FacilityPathSaveRequest request) {
        facilityPathService.save(findById(facilityId), request.name(), request.type(), request.path());
    }

    @Transactional
    public void updatePath(Long facilityId, Long pathId, FacilityPathUpdateRequest request) {
        findById(facilityId);
        facilityPathService.update(pathId, request.name(), request.type(), request.path());
    }

    @Transactional
    public void deletePath(Long facilityId, Long pathId) {
        findById(facilityId);
        facilityPathService.delete(pathId);
    }

    @Transactional
    public void updateLocation(Long facilityId, FacilityLocationUpdateRequest request) {
        Facility facility = findById(facilityId);
        facility.updatePosition(
                FacilityPosition.builder()
                        .lon(request.lon())
                        .lat(request.lat())
                        .locationMeta(request.locationMeta())
                        .build());
    }

    @Transactional
    public void updateFloor(Long facilityId, FacilityFloorUpdateRequest request) {
        Facility facility = findById(facilityId);
        floorService.update(facility, request.floors());
    }
}
