package com.pluxity.facility.facility.handler;

import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.facility.FacilityType;
import com.pluxity.facility.facility.dto.details.FacilityDetailsDto;

public interface FacilityHandler {

    FacilityType getType();

    void saveDetails(Facility facility, FacilityDetailsDto detailsDto);

    void updateDetails(Facility facility, FacilityDetailsDto detailsDto);

    FacilityDetailsDto getDetails(Facility facility);

    void deleteDetails(Facility facility);
}