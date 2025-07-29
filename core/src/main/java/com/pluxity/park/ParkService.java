package com.pluxity.park;

import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityService;
import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.MappingUtils;
import com.pluxity.global.utils.SortUtils;
import com.pluxity.park.dto.ParkCreateRequest;
import com.pluxity.park.dto.ParkResponse;
import com.pluxity.park.dto.ParkUpdateRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParkService {

    private final FileService fileService;
    private final FacilityService facilityService;
    private final ParkRepository parkRepository;

    @Transactional
    public Long save(ParkCreateRequest request) {

        Park park =
                Park.builder()
                        .name(request.facility().name())
                        .code(request.facility().code())
                        .description(request.facility().description())
                        .drawingFileId(request.facility().drawingFileId())
                        .thumbnailFileId(request.facility().thumbnailFileId())
                        .boundary(request.boundary())
                        .build();

        Facility saved = facilityService.save(park, request.facility());

        return saved.getId();
    }

    @Transactional(readOnly = true)
    public List<ParkResponse> findAll() {
        List<Park> parks = parkRepository.findAll(SortUtils.getOrderByCreatedAtDesc());
        Map<Long, FileResponse> fileMap =
                MappingUtils.getFileMapByIds(
                        parks, v -> Stream.of(v.getDrawingFileId(), v.getThumbnailFileId()), fileService);

        return parks.stream()
                .map(
                        park ->
                                ParkResponse.builder()
                                        .facility(
                                                FacilityResponse.from(
                                                        park,
                                                        fileMap.get(park.getDrawingFileId()),
                                                        fileMap.get(park.getThumbnailFileId())))
                                        .boundary(park.getBoundary())
                                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public ParkResponse findById(Long id) {
        Park park = (Park) facilityService.findById(id);

        return ParkResponse.builder()
                .facility(
                        FacilityResponse.from(
                                park,
                                fileService.getFileResponse(park.getDrawingFileId()),
                                fileService.getFileResponse(park.getThumbnailFileId())))
                .boundary(park.getBoundary())
                .build();
    }

    private Park findPark(Long id) {
        return parkRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PARK, id));
    }

    @Transactional
    public void update(Long id, ParkUpdateRequest request) {
        Park park = findPark(id);
        facilityService.putUpdate(id, request.facility());
        park.updateBoundary(request.boundary());
    }

    @Transactional
    public void delete(Long id) {
        findPark(id);
        facilityService.deleteFacility(id);
    }

    @Transactional(readOnly = true)
    public List<FacilityResponse> findAllFacilities() {
        List<Park> parks = parkRepository.findAll(SortUtils.getOrderByCreatedAtDesc());
        return MappingUtils.mapWithFiles(parks, fileService);
    }
}
