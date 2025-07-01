package com.pluxity.facility.facility.handler;

import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityType;
import com.pluxity.facility.facility.StationInfo;
import com.pluxity.facility.facility.dto.details.FacilityDetailsDto;
import com.pluxity.facility.facility.dto.details.StationDetailsDto;
import com.pluxity.facility.facility.repository.StationInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StationHandler implements FacilityHandler {

    private final StationInfoRepository stationInfoRepository;

    @Override
    public FacilityType getType() {
        return FacilityType.STATION;
    }

    @Override
    public void saveDetails(Facility facility, FacilityDetailsDto detailsDto) {
        StationDetailsDto stationDetails = (StationDetailsDto) detailsDto;
        StationInfo stationInfo = new StationInfo(facility, stationDetails.lineName());
        stationInfoRepository.save(stationInfo);
    }

    @Override
    public void updateDetails(Facility facility, FacilityDetailsDto detailsDto) {
        StationDetailsDto stationDetails = (StationDetailsDto) detailsDto;
        stationInfoRepository.findById(facility.getId()).ifPresent(info ->
            info.updateLineName(stationDetails.lineName())
        );
    }

    @Override
    public FacilityDetailsDto getDetails(Facility facility) {
        return stationInfoRepository.findById(facility.getId())
            .map(StationDetailsDto::new)
            .orElse(null);
    }

    @Override
    public void deleteDetails(Facility facility) {
        stationInfoRepository.deleteById(facility.getId());
    }
} 