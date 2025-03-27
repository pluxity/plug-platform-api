package com.pluxity.building.service;

import com.pluxity.building.dto.BuildingCreateRequest;
import com.pluxity.building.dto.BuildingResponse;
import com.pluxity.building.dto.BuildingUpdateRequest;
import com.pluxity.building.entity.Building;
import com.pluxity.building.repository.BuildingRepository;
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
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final FileService fileService;

    @Transactional
    public Long createBuilding(BuildingCreateRequest request) {
        try {
            Building building = Building.builder()
                    .name(request.name())
                    .description(request.description())
                    .fileId(request.fileId())
                    .thumbnailId(request.thumbnailId())
                    .build();
            
            Building savedBuilding = buildingRepository.save(building);
            
            FileResponse fileResponse = null;
            if (request.fileId() != null) {
                String filePath = "buildings/" + savedBuilding.getId() + "/file";
                FileEntity fileEntity = fileService.finalizeUpload(request.fileId(), filePath);
                String fileUrl = fileService.generateFileUrl(fileEntity.getFilePath());
                fileResponse = FileResponse.from(fileEntity, fileUrl);
            }
            
            FileResponse thumbnailResponse = null;
            if (request.thumbnailId() != null) {
                String thumbnailPath = "buildings/" + savedBuilding.getId() + "/thumbnail";
                FileEntity thumbnailEntity = fileService.finalizeUpload(request.thumbnailId(), thumbnailPath);
                String thumbnailUrl = fileService.generateFileUrl(thumbnailEntity.getFilePath());
                thumbnailResponse = FileResponse.from(thumbnailEntity, thumbnailUrl);
            }
            
            return savedBuilding.getId();
        } catch (Exception e) {
            log.error("Building creation failed: {}", e.getMessage());
            throw new CustomException("Building creation failed", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public BuildingResponse getBuilding(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new CustomException("Building not found", HttpStatus.NOT_FOUND, "해당 건물을 찾을 수 없습니다."));
        
        FileResponse fileResponse = null;
        if (building.getFileId() != null) {
            FileEntity fileEntity = fileService.getFile(building.getFileId());
            String fileUrl = fileService.generateFileUrl(fileEntity.getFilePath());
            fileResponse = FileResponse.from(fileEntity, fileUrl);
        }
        
        FileResponse thumbnailResponse = null;
        if (building.getThumbnailId() != null) {
            FileEntity thumbnailEntity = fileService.getFile(building.getThumbnailId());
            String thumbnailUrl = fileService.generateFileUrl(thumbnailEntity.getFilePath());
            thumbnailResponse = FileResponse.from(thumbnailEntity, thumbnailUrl);
        }
        
        return BuildingResponse.from(building, fileResponse, thumbnailResponse);
    }

    @Transactional(readOnly = true)
    public List<BuildingResponse> getAllBuildings() {
        List<Building> buildings = buildingRepository.findAll();
        
        return buildings.stream()
                .map(building -> {
                    FileResponse fileResponse = null;
                    if (building.getFileId() != null) {
                        FileEntity fileEntity = fileService.getFile(building.getFileId());
                        String fileUrl = fileService.generateFileUrl(fileEntity.getFilePath());
                        fileResponse = FileResponse.from(fileEntity, fileUrl);
                    }
                    
                    FileResponse thumbnailResponse = null;
                    if (building.getThumbnailId() != null) {
                        FileEntity thumbnailEntity = fileService.getFile(building.getThumbnailId());
                        String thumbnailUrl = fileService.generateFileUrl(thumbnailEntity.getFilePath());
                        thumbnailResponse = FileResponse.from(thumbnailEntity, thumbnailUrl);
                    }
                    
                    return BuildingResponse.from(building, fileResponse, thumbnailResponse);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public BuildingResponse updateBuilding(Long id, BuildingUpdateRequest request) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new CustomException("Building not found", HttpStatus.NOT_FOUND, "해당 건물을 찾을 수 없습니다."));
        
        building.update(request.name(), request.description());
        
        FileResponse fileResponse = null;
        if (request.fileId() != null) {
            String filePath = "buildings/" + building.getId() + "/file";
            FileEntity fileEntity = fileService.finalizeUpload(request.fileId(), filePath);
            building.updateFileId(fileEntity.getId());
            String fileUrl = fileService.generateFileUrl(fileEntity.getFilePath());
            fileResponse = FileResponse.from(fileEntity, fileUrl);
        } else if (building.getFileId() != null) {
            FileEntity fileEntity = fileService.getFile(building.getFileId());
            String fileUrl = fileService.generateFileUrl(fileEntity.getFilePath());
            fileResponse = FileResponse.from(fileEntity, fileUrl);
        }
        
        FileResponse thumbnailResponse = null;
        if (request.thumbnailId() != null) {
            String thumbnailPath = "buildings/" + building.getId() + "/thumbnail";
            FileEntity thumbnailEntity = fileService.finalizeUpload(request.thumbnailId(), thumbnailPath);
            building.updateThumbnailId(thumbnailEntity.getId());
            String thumbnailUrl = fileService.generateFileUrl(thumbnailEntity.getFilePath());
            thumbnailResponse = FileResponse.from(thumbnailEntity, thumbnailUrl);
        } else if (building.getThumbnailId() != null) {
            FileEntity thumbnailEntity = fileService.getFile(building.getThumbnailId());
            String thumbnailUrl = fileService.generateFileUrl(thumbnailEntity.getFilePath());
            thumbnailResponse = FileResponse.from(thumbnailEntity, thumbnailUrl);
        }
        
        Building updatedBuilding = buildingRepository.save(building);
        return BuildingResponse.from(updatedBuilding, fileResponse, thumbnailResponse);
    }

    @Transactional
    public void deleteBuilding(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new CustomException("Building not found", HttpStatus.NOT_FOUND, "해당 건물을 찾을 수 없습니다."));
        
        buildingRepository.delete(building);
    }
}
