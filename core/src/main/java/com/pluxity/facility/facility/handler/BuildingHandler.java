package com.pluxity.facility.facility.handler;

import com.pluxity.facility.facility.BuildingInfo;
import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityType;
import com.pluxity.facility.facility.dto.details.BuildingDetailsDto;
import com.pluxity.facility.facility.dto.details.FacilityDetailsDto;
import com.pluxity.facility.facility.repository.BuildingInfoRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BuildingHandler implements FacilityHandler {

    private final BuildingInfoRepository buildingInfoRepository;

    @Override
    public FacilityType getType() {
        return FacilityType.BUILDING;
    }

    @Override
    public void saveDetails(Facility facility, FacilityDetailsDto detailsDto) {
        BuildingDetailsDto buildingDetails = (BuildingDetailsDto) detailsDto;
        BuildingInfo buildingInfo = new BuildingInfo(facility, buildingDetails.floorCount(), buildingDetails.address());
        buildingInfoRepository.save(buildingInfo);
    }

    @Override
    public void updateDetails(Facility facility, FacilityDetailsDto detailsDto) {
        BuildingDetailsDto buildingDetails = (BuildingDetailsDto) detailsDto;
        buildingInfoRepository.findById(facility.getId()).ifPresent(info -> {
            info.updateFloorCount(buildingDetails.floorCount());
            info.updateAddress(buildingDetails.address());
        });
    }

    @Override
    public FacilityDetailsDto getDetails(Facility facility) {
        return buildingInfoRepository.findById(facility.getId())
            .map(BuildingDetailsDto::new)
            .orElse(null);
    }

    @Override
    public void deleteDetails(Facility facility) {
        buildingInfoRepository.deleteById(facility.getId());
    }
} 