package com.pluxity.facility.service;

import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.dto.PanoramaCreateRequest;
import com.pluxity.facility.dto.PanoramaResponse;
import com.pluxity.facility.dto.PanoramaUpdateRequest;
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
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();

        Facility saved = facilityService.save(panorama, request.facility());

        return saved.getId();
    }

    @Transactional(readOnly = true)
    public List<PanoramaResponse> findAll() {
        List<Panorama> panoramas = repository.findAll();

        return panoramas.stream()
                .map(panorama ->  PanoramaResponse.builder()
                        .facility(FacilityResponse.from(panorama, fileService.getFileResponse(panorama.getDrawingFileId()), fileService.getFileResponse(panorama.getThumbnailFileId())))
                        .address(panorama.getAddress())
                        .latitude(panorama.getLatitude())
                        .longitude(panorama.getLongitude())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public PanoramaResponse findById(Long id) {
        Panorama panorama = (Panorama) facilityService.findById(id);

        return PanoramaResponse.builder()
                .facility(FacilityResponse.from(panorama, fileService.getFileResponse(panorama.getDrawingFileId()), fileService.getFileResponse(panorama.getThumbnailFileId())))
                .address(panorama.getAddress())
                .latitude(panorama.getLatitude())
                .longitude(panorama.getLongitude())
                .build();
    }

    @Transactional
    public void update(Long id, PanoramaUpdateRequest request) {

        var panorama = Panorama.builder()
                .name(request.name())
                .description(request.description())
                .build();

        facilityService.update(id, panorama);
    }


    @Transactional
    public void delete(Long id) {
        facilityService.deleteFacility(id);
    }

}
