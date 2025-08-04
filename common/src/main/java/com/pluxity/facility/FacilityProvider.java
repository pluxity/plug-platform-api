package com.pluxity.facility;

import com.pluxity.facility.dto.FacilityApiType;
import com.pluxity.facility.dto.FacilityResponse;
import java.util.List;

public interface FacilityProvider {
    FacilityApiType getFacilityApiType();

    List<FacilityResponse> getAllFacilities();
}
