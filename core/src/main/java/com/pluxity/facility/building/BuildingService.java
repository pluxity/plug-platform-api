package com.pluxity.facility.building;

import com.pluxity.facility.building.dto.BuildingCreateRequest;
import com.pluxity.facility.building.dto.BuildingResponse;
import com.pluxity.facility.building.dto.BuildingUpdateRequest;
import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityService;
import com.pluxity.facility.facility.dto.FacilityHistoryResponse;
import com.pluxity.facility.facility.dto.FacilityResponse;
import com.pluxity.facility.floor.dto.FloorRequest;
import com.pluxity.facility.floor.dto.FloorResponse;
import com.pluxity.facility.strategy.FloorStrategy;
import com.pluxity.file.service.FileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final FileService fileService;
    private final FacilityService facilityService;
    private final FloorStrategy floorStrategy;
    private final BuildingRepository repository;

    @Transactional
    public Long save(BuildingCreateRequest request) {

        Building building =
                Building.builder()
                        .name(request.facility().name())
                        .description(request.facility().description())
                        .build();

        Facility saved = facilityService.save(building, request.facility());

        if (request.floors() != null) {
            for (FloorRequest floorRequest : request.floors()) {
                floorStrategy.save(saved, floorRequest);
            }
        }

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
                                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public BuildingResponse findById(Long id) {
        Building building = (Building) facilityService.findById(id);
        List<FloorResponse> floorResponses = floorStrategy.findAllByFacility(building);

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
        var building =
                Building.builder().name(request.name()).description(request.description()).build();

        facilityService.update(id, building);
    }

    @Transactional
    public void delete(Long id) {
        var building = facilityService.findById(id);
        floorStrategy.delete(building);
        facilityService.deleteFacility(id);
    }
}
