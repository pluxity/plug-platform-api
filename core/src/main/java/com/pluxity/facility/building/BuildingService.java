package com.pluxity.facility.building;

import com.pluxity.facility.building.dto.BuildingCreateRequest;
import com.pluxity.facility.building.dto.BuildingResponse;
import com.pluxity.facility.building.dto.BuildingUpdateRequest;
import com.pluxity.facility.building.mapper.BuildingMapper; // Added import
import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityService;
import com.pluxity.facility.facility.dto.FacilityHistoryResponse;
// import com.pluxity.facility.facility.dto.FacilityResponse; // No longer needed for manual mapping here
import com.pluxity.facility.floor.dto.FloorRequest;
import com.pluxity.facility.floor.dto.FloorResponse;
import com.pluxity.facility.strategy.FloorStrategy;
import com.pluxity.global.exception.CustomException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final FacilityService facilityService;
    private final FloorStrategy floorStrategy;
    private final BuildingRepository repository;
    private final BuildingMapper buildingMapper; // Added mapper

    @Transactional
    public Long save(BuildingCreateRequest request) {
        Building building = new Building(
                request.facility().name(),
                request.facility().description()
        );
        facilityService.save(building.getFacility(), request.facility());
        Building savedBuilding = repository.save(building);

        if (request.floors() != null) {
            for (FloorRequest floorRequest : request.floors()) {
                floorStrategy.save(savedBuilding.getFacility(), floorRequest);
            }
        }
        return savedBuilding.getId();
    }

    @Transactional(readOnly = true)
    public List<BuildingResponse> findAll() {
        List<Building> buildings = repository.findAll();
        return buildings.stream()
                .map(building -> {
                    BuildingResponse buildingDto = buildingMapper.toBuildingResponse(building);
                    // Floors are ignored by mapper, set them manually
                    List<FloorResponse> floorResponses = floorStrategy.findAllByFacility(building.getFacility());
                    return new BuildingResponse(buildingDto.id(), buildingDto.facility(), floorResponses);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BuildingResponse findById(Long id) {
        Building building = repository.findById(id)
                .orElseThrow(() -> new CustomException("Building not found with id: " + id, HttpStatus.NOT_FOUND, "빌딩을 찾을 수 없습니다."));

        BuildingResponse buildingDto = buildingMapper.toBuildingResponse(building);
        // Floors are ignored by mapper, set them manually
        List<FloorResponse> floorResponses = floorStrategy.findAllByFacility(building.getFacility());
        return new BuildingResponse(buildingDto.id(), buildingDto.facility(), floorResponses);
    }

    @Transactional(readOnly = true)
    public List<FacilityHistoryResponse> findFacilityHistories(Long id) { // id is Building's ID
        Building building = repository.findById(id)
                .orElseThrow(() -> new CustomException("Building not found with id: " + id, HttpStatus.NOT_FOUND, "빌딩을 찾을 수 없습니다."));
        return facilityService.findFacilityHistories(building.getFacility().getId());
    }

    @Transactional
    public void update(Long id, BuildingUpdateRequest request) { // id is Building's ID
        Building buildingToUpdate = repository.findById(id)
                .orElseThrow(() -> new CustomException("Building not found with id: " + id, HttpStatus.NOT_FOUND, "빌딩을 찾을 수 없습니다."));

        if (request.facility() != null) {
            facilityService.update(buildingToUpdate.getFacility().getId(), request.facility());
        }

        // Update floors if provided
        if (request.floors() != null) {
            // This typically involves more complex logic:
            // 1. Remove existing floors not in the new list (or all existing floors)
            // 2. Update existing floors that are also in the new list
            // 3. Add new floors from the new list.
            // For simplicity, let's assume a full replacement strategy if floors are provided.
            floorStrategy.delete(buildingToUpdate.getFacility()); // Delete all existing floors for this facility
            for (FloorRequest floorRequest : request.floors()) {
                floorStrategy.save(buildingToUpdate.getFacility(), floorRequest); // Add new floors
            }
        }
        repository.save(buildingToUpdate);
    }

    @Transactional
    public void delete(Long id) { // id is Building's ID
        Building buildingToDelete = repository.findById(id)
                .orElseThrow(() -> new CustomException("Building not found with id: " + id, HttpStatus.NOT_FOUND, "빌딩을 찾을 수 없습니다."));

        floorStrategy.delete(buildingToDelete.getFacility());
        repository.delete(buildingToDelete);
    }
}
