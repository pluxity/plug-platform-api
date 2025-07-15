package com.pluxity.building;

import com.pluxity.building.dto.BuildingCreateRequest;
import com.pluxity.building.dto.BuildingResponse;
import com.pluxity.building.dto.BuildingUpdateRequest;
import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityService;
import com.pluxity.facility.dto.FacilityHistoryResponse;
import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorResponse;
import com.pluxity.facility.strategy.FloorService;
import com.pluxity.file.service.FileService;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.utils.FacilityMappingUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final FileService fileService;
    private final FacilityService facilityService;
    private final FloorService floorService;
    private final BuildingRepository repository;

    @Transactional
    public Long save(BuildingCreateRequest request) {

        Building building =
                Building.builder()
                        .name(request.facility().name())
                        .description(request.facility().description())
                        .build();

        Facility saved = facilityService.save(building, request.facility());

        floorService.save(saved, request.floors());
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public List<BuildingResponse> findAll() {
        List<Building> buildings = repository.findAll();

        return buildings.stream()
                .map(
                        building ->
                                BuildingResponse.builder()
                                        .facility(
                                                FacilityResponse.from(
                                                        building,
                                                        fileService.getFileResponse(building.getDrawingFileId()),
                                                        fileService.getFileResponse(building.getThumbnailFileId())))
                                        .floors(floorService.findAllByFacility(building))
                                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public BuildingResponse findById(Long id) {
        Building building = findBuilding(id);
        List<FloorResponse> floorResponses = floorService.findAllByFacility(building);

        return BuildingResponse.builder()
                .facility(
                        FacilityResponse.from(
                                building,
                                fileService.getFileResponse(building.getDrawingFileId()),
                                fileService.getFileResponse(building.getThumbnailFileId())))
                .floors(floorResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public List<FacilityHistoryResponse> findFacilityHistories(Long id) {
        return facilityService.findFacilityHistories(id);
    }

    @Transactional
    public void update(Long id, BuildingUpdateRequest request) {
        Building building = findBuilding(id);

        facilityService.update(id, request.facility());

        floorService.update(building, request.floors());
    }

    private Building findBuilding(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_BUILDING, id));
    }

    @Transactional
    public void delete(Long id) {
        var building = facilityService.findById(id);
        floorService.delete(building);
        facilityService.deleteFacility(id);
    }

    @Transactional(readOnly = true)
    public List<FacilityResponse> findAllFacilities() {
        List<Building> buildings = repository.findAll();
        return FacilityMappingUtil.mapWithFiles(buildings, fileService);
    }
}
