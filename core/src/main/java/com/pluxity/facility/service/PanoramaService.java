package com.pluxity.facility.service;

import com.pluxity.facility.dto.*;
import com.pluxity.facility.entity.Facility;
import com.pluxity.facility.entity.Panorama;
import com.pluxity.facility.repository.PanoramaRepository;
import com.pluxity.facility.strategy.LocationStrategy;
import com.pluxity.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PanoramaService {

    private final FacilityService facilityService;

    private final FileService fileService;

    private final LocationStrategy locationStrategy;

    private final PanoramaRepository repository;

    @Transactional
    public Long save(PanoramaCreateRequest request) {

        Panorama panorama = Panorama.builder()
                .name(request.facility().name())
                .description(request.facility().description())
                .build();

        Facility facility = facilityService.save(panorama, request.facility());

        if(request.locationRequest() != null) {
            locationStrategy.save(facility, request.locationRequest());
        }

        return facility.getId();
    }

    @Transactional(readOnly = true)
    public List<PanoramaResponse> findAll() {
        List<Panorama> panoramas = repository.findAll();

        return panoramas.stream()
                .map(panorama ->  PanoramaResponse.builder()
                        .facility(FacilityResponse.from(panorama, fileService.getFileResponse(panorama.getDrawingFileId()), fileService.getFileResponse(panorama.getThumbnailFileId())))
                        .location(LocationResponse.from(panorama))
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public PanoramaResponse findById(Long id) {
        Panorama panorama = (Panorama) facilityService.findById(id);

        return PanoramaResponse.builder()
                .facility(FacilityResponse.from(panorama, fileService.getFileResponse(panorama.getDrawingFileId()), fileService.getFileResponse(panorama.getThumbnailFileId())))
                .location(LocationResponse.from(panorama))
                .build();
    }

    @Transactional
    public void update(Long id, PanoramaUpdateRequest request) {

        var panorama = Panorama.builder()
                .name(request.name())
                .description(request.description())
                .build();

        facilityService.update(id, panorama);

        if(request.locationRequest() != null) {
            locationStrategy.update(panorama, request.locationRequest());
        }
    }


    @Transactional
    public void delete(Long id) {
        facilityService.deleteFacility(id);
    }

}
