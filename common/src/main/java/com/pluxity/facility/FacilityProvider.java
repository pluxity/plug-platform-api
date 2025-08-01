package com.pluxity.facility;

import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.dto.FacilityResponseKey;
import java.util.List;

public interface FacilityProvider {
    FacilityResponseKey getResponseKey();

    List<FacilityResponse> getAllFacilities();
}
