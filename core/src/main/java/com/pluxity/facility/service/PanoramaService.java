package com.pluxity.facility.service;

import com.pluxity.facility.strategy.LocationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PanoramaService {

    private final FacilityService facilityService;

    private final LocationStrategy locationStrategy;

}
