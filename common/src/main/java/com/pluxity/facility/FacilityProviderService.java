package com.pluxity.facility;

import com.pluxity.facility.dto.FacilityResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FacilityProviderService {

    private final List<FacilityProvider> facilityProviders;

    public Map<String, List<FacilityResponse>> findAllFacilities() {
        Map<String, List<FacilityResponse>> ret = new HashMap<>();
        for (FacilityProvider facilityProvider : facilityProviders) {
            ret.put(facilityProvider.getResponseKey().getKey(), facilityProvider.getAllFacilities());
        }
        return ret;
    }
}
