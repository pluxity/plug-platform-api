package com.pluxity.park;

import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityService;
import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.global.utils.SortUtils;
import com.pluxity.park.dto.ParkCreateRequest;
import com.pluxity.park.dto.ParkResponse;
import com.pluxity.park.dto.ParkUpdateRequest;
import java.util.List;
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

        return parks.stream()
                .map(
                        park ->
                                ParkResponse.builder()
                                        .facility(
                                                FacilityResponse.from(
                                                        park,
                                                        fileService.getFileResponse(park.getDrawingFileId()),
                                                        fileService.getFileResponse(park.getThumbnailFileId())))
                                        .boundary(park.getBoundary())
                                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public ParkResponse findById(Long id) {
        Park park = findPark(id);

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

        facilityService.update(id, request.facility());
        park.update(request.boundary());
    }

    @Transactional
    public void delete(Long id) {
        Park park = findPark(id);
        facilityService.deleteFacility(id);
    }
}
