package com.pluxity.facility.facility.handler;

import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityType;
import com.pluxity.facility.facility.PanoramaInfo;
import com.pluxity.facility.facility.dto.details.FacilityDetailsDto;
import com.pluxity.facility.facility.dto.details.PanoramaDetailsDto;
import com.pluxity.facility.facility.repository.PanoramaInfoRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PanoramaHandler implements FacilityHandler {

    private final PanoramaInfoRepository panoramaInfoRepository;

    @Override
    public FacilityType getType() {
        return FacilityType.PANORAMA;
    }

    @Override
    public void saveDetails(Facility facility, FacilityDetailsDto detailsDto) {
        PanoramaDetailsDto panoramaDetails = (PanoramaDetailsDto) detailsDto;
        PanoramaInfo panoramaInfo = new PanoramaInfo(facility, panoramaDetails.panoramaUrl());
        panoramaInfoRepository.save(panoramaInfo);
    }

    @Override
    public void updateDetails(Facility facility, FacilityDetailsDto detailsDto) {
        PanoramaDetailsDto panoramaDetails = (PanoramaDetailsDto) detailsDto;
        panoramaInfoRepository.findById(facility.getId()).ifPresent(info ->
            info.updatePanoramaUrl(panoramaDetails.panoramaUrl())
        );
    }

    @Override
    public FacilityDetailsDto getDetails(Facility facility) {
        return panoramaInfoRepository.findById(facility.getId())
            .map(info -> new PanoramaDetailsDto(info))
            .orElse(null);
    }

    @Override
    public void deleteDetails(Facility facility) {
        panoramaInfoRepository.deleteById(facility.getId());
    }
} 