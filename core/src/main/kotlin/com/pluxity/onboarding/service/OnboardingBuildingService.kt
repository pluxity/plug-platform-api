package com.pluxity.onboarding.service

import com.pluxity.building.Building
import com.pluxity.onboarding.dto.OnboardingFacilityRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class OnboardingBuildingService(
    private val facilityService: OnboardingFacilityService
) {
    fun save(request: OnboardingFacilityRequest): Long {

        val building = Building(
            name = request.name,
            description = request.description
        )
        return facilityService.save(building, request).id!!
    }
}