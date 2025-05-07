package com.pluxity.facility.service;

import com.pluxity.facility.domain.FacilityType;
import com.pluxity.facility.dto.*;
import com.pluxity.facility.entity.Building;
import com.pluxity.facility.repository.BuildingRepository;
import com.pluxity.facility.strategy.FloorStrategy;
import com.pluxity.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final FileService fileService;
    private final FacilityService facilityService;
    private final FloorStrategy floorStrategy;
    private final BuildingRepository repository;

    @Transactional
    public Long save(BuildingCreateRequest request) {
        Building building = Building.builder()
                .name(request.facility().name())
                .description(request.facility().description())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .facilityType(FacilityType.BUILDING)
                .build();

        Building saved = repository.save(building);
        
        facilityService.save(building.getFacility(), request.facility());

        for(FloorRequest floorRequest : request.floors()) {
            floorStrategy.save(building.getFacility(), floorRequest);
        }

        return saved.getId();
    }

    @Transactional(readOnly = true)
    public List<BuildingResponse> findAll() {
        List<Building> buildings = repository.findAll();

        return buildings.stream()
                .map(building -> BuildingResponse.builder()
                        .facility(FacilityResponse.from(building.getFacility(), 
                                fileService.getFileResponse(building.getDrawingFile()), 
                                fileService.getFileResponse(building.getThumbnailFile())))
                        .address(building.getAddress())
                        .latitude(building.getLatitude())
                        .longitude(building.getLongitude())
                        .floors(floorStrategy.findAllByFacility(building.getFacility()))
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public BuildingResponse findById(Long id) {
        Building building = repository.findById(id)
                .orElseThrow(NotFoundBuildingException(id));
                
        List<FloorResponse> floorResponses = floorStrategy.findAllByFacility(building.getFacility());

        return BuildingResponse.builder()
                .facility(FacilityResponse.from(building.getFacility(), 
                        fileService.getFileResponse(building.getDrawingFile()), 
                        fileService.getFileResponse(building.getThumbnailFile())))
                .address(building.getAddress())
                .latitude(building.getLatitude())
                .longitude(building.getLongitude())
                .floors(floorResponses)
                .build();
    }

    @Transactional
    public void update(Long id, BuildingUpdateRequest request) {
        Building building = repository.findById(id)
                .orElseThrow(NotFoundBuildingException(id));
        
        if (request.name() != null) {
            building.updateName(request.name());
        }
        
        if (request.description() != null) {
            building.updateDescription(request.description());
        }
        
        repository.save(building);
    }

    @Transactional
    public void delete(Long id) {
        Building building = repository.findById(id)
                .orElseThrow(NotFoundBuildingException(id));
                
        floorStrategy.delete(building.getFacility());
        
        repository.deleteById(id);
    }

    private static Supplier<IllegalArgumentException> NotFoundBuildingException(Long id) {
        return () -> new IllegalArgumentException("존재하지 않는 빌딩입니다. ID: " + id);
    }
}
