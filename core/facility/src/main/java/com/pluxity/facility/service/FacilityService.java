package com.pluxity.facility.service;

import com.pluxity.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.dto.FacilityUpdateRequest;
import com.pluxity.facility.entity.Facility;
import com.pluxity.facility.repository.FacilityRepository;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final FileService fileService;

    @Transactional
    public Long createFacility(FacilityCreateRequest request) {
        try {
            Facility facility = Facility.builder()
                    .name(request.name())
                    .description(request.description())
                    .fileId(request.fileId())
                    .thumbnailId(request.thumbnailId())
                    .build();
            
            Facility savedFacility = facilityRepository.save(facility);
            
            String filePath = "facilities/" + savedFacility.getId() + "/";
            if (request.fileId() != null) {
                fileService.finalizeUpload(request.fileId(), filePath);
            }
            
            if (request.thumbnailId() != null) {
                fileService.finalizeUpload(request.thumbnailId(), filePath);
            }

            return savedFacility.getId();
        } catch (Exception e) {
            log.error("Facility creation failed: {}", e.getMessage());
            throw new CustomException("Facility creation failed", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public FacilityResponse getFacility(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new CustomException("Facility not found", HttpStatus.NOT_FOUND, "해당 건물을 찾을 수 없습니다."));
        
        FileResponse fileResponse = fileService.getFileResponse(facility.getFileId());
        FileResponse thumbnailResponse = fileService.getFileResponse(facility.getThumbnailId());
        
        return FacilityResponse.from(facility, fileResponse, thumbnailResponse);
    }

    @Transactional(readOnly = true)
    public List<FacilityResponse> getAllFacilities() {
        List<Facility> facilities = facilityRepository.findAll();
        
        return facilities.stream()
                .map(facility -> {
                    FileResponse fileResponse = fileService.getFileResponse(facility.getFileId());
                    FileResponse thumbnailResponse = fileService.getFileResponse(facility.getThumbnailId());
                    
                    return FacilityResponse.from(facility, fileResponse, thumbnailResponse);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateFacility(Long id, FacilityUpdateRequest request) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new CustomException("Facility not found", HttpStatus.NOT_FOUND, "해당 시설을 찾을 수 없습니다."));
        
        facility.update(request.name(), request.description());
        
        String filePath = "facilities/" + facility.getId() + "/";
        if (request.fileId() != null) {
            FileEntity fileEntity = fileService.finalizeUpload(request.fileId(), filePath);
            facility.updateFileId(fileEntity.getId());
            fileService.getFileResponse(fileEntity);
        } else if (facility.getFileId() != null) {
            fileService.getFileResponse(facility.getFileId());
        }
        
        if (request.thumbnailId() != null) {
            FileEntity thumbnailEntity = fileService.finalizeUpload(request.thumbnailId(), filePath);
            facility.updateThumbnailId(thumbnailEntity.getId());
            fileService.getFileResponse(thumbnailEntity);
        } else if (facility.getThumbnailId() != null) {
            fileService.getFileResponse(facility.getThumbnailId());
        }
        
        facilityRepository.save(facility);
    }

    @Transactional
    public void deleteFacility(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new CustomException("Facility not found", HttpStatus.NOT_FOUND, "해당 시설을 찾을 수 없습니다."));
        
        facilityRepository.delete(facility);
    }
}
