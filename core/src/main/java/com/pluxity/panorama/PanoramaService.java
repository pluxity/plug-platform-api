package com.pluxity.panorama;

import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityService;
import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.strategy.FloorService;
import com.pluxity.file.service.FileService;
import com.pluxity.global.utils.FacilityMappingUtil;
import com.pluxity.panorama.dto.PanoramaCreateRequest;
import com.pluxity.panorama.dto.PanoramaResponse;
import com.pluxity.panorama.dto.PanoramaUpdateRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PanoramaService {

    private final FacilityService facilityService;

    private final FileService fileService;

    private final FloorService floorService;

    private final PanoramaRepository repository;

    @Transactional
    public Long save(PanoramaCreateRequest request) {

        Panorama panorama =
                Panorama.builder()
                        .name(request.facility().name())
                        .description(request.facility().description())
                        .build();

        Facility facility = facilityService.save(panorama, request.facility());

        //        if (request.locationRequest() != null) {
        //            floorStrategy.save(facility, request.locationRequest());
        //        }

        return facility.getId();
    }

    @Transactional(readOnly = true)
    public List<PanoramaResponse> findAll() {
        List<Panorama> panoramas = repository.findAll();

        return panoramas.stream()
                .map(
                        panorama ->
                                PanoramaResponse.builder()
                                        .facility(
                                                FacilityResponse.from(
                                                        panorama,
                                                        fileService.getFileResponse(panorama.getDrawingFileId()),
                                                        fileService.getFileResponse(panorama.getThumbnailFileId())))
                                        //
                                        // .location(LocationResponse.from(panorama))
                                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public PanoramaResponse findById(Long id) {
        Panorama panorama = (Panorama) facilityService.findById(id);

        return PanoramaResponse.builder()
                .facility(
                        FacilityResponse.from(
                                panorama,
                                fileService.getFileResponse(panorama.getDrawingFileId()),
                                fileService.getFileResponse(panorama.getThumbnailFileId())))
                //                .location(LocationResponse.from(panorama))
                .build();
    }

    @Transactional
    public void update(Long id, PanoramaUpdateRequest request) {
        if(request.facility() != null) {
            facilityService.update(id, request.facility());
        }

        //        if (request.locationRequest() != null) {
        //            Panorama savedPanorama = repository.findById(id).orElseThrow();
        //            floorStrategy.update(savedPanorama, request.locationRequest());
        //        }
    }

    @Transactional
    public void delete(Long id) {
        facilityService.deleteFacility(id);
    }

    @Transactional(readOnly = true)
    public List<FacilityResponse> findAllFacilities() {
        List<Panorama> panoramas = repository.findAll();
        return FacilityMappingUtil.mapWithFiles(panoramas, fileService);
    }
}
