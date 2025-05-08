package com.pluxity.facility.service;

import com.pluxity.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.entity.Facility;
import com.pluxity.facility.repository.FacilityRepository;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final FileService fileService;

    private final String PREFIX = "facilities/";

    @Transactional
    protected Facility save(Facility facility, FacilityCreateRequest request) {
        try {

            Facility savedFacility = facilityRepository.save(facility);
            
            String filePath = PREFIX + savedFacility.getId() + "/";
            if (request.drawingFileId() != null) {
                facility.updateDrawingFileId(fileService.finalizeUpload(request.drawingFileId(), filePath));
            }
            
            if (request.thumbnailFileId() != null) {
                facility.updateThumbnailFileId(fileService.finalizeUpload(request.thumbnailFileId(), filePath));
            }

            return savedFacility;
        } catch (Exception e) {
            log.error("Facility creation failed: {}", e.getMessage());
            throw new CustomException("Facility creation failed", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    protected Facility findById(Long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new CustomException("Facility not found", HttpStatus.NOT_FOUND, "해당 시설을 찾을 수 없습니다."));
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
    protected void update(Long id, Facility newFacility) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new CustomException("Facility not found", HttpStatus.NOT_FOUND, "해당 시설을 찾을 수 없습니다."));
        facility.update(newFacility);

        facilityRepository.save(facility);
    }

    @Transactional
    protected void deleteFacility(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new CustomException("Facility not found", HttpStatus.NOT_FOUND, "해당 시설을 찾을 수 없습니다."));
        
        facilityRepository.delete(facility);
    }
}
