package com.pluxity.onboarding.service

import com.pluxity.onboarding.dto.OnboardingFacilityRequest
import com.pluxity.station.Station
import org.springframework.stereotype.Service

@Service
class OnboardingBuildingService(
    private val facilityService: OnboardingFacilityService,
) {
    fun save(request: OnboardingFacilityRequest): Long {
        val building =
            Station(
                name = request.name,
                description = request.description,
            )
        return facilityService.save(building, request).id!!
    }
}
