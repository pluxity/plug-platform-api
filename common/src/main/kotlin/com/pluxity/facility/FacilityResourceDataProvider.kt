package com.pluxity.facility

import com.pluxity.permission.ResourceDataProvider
import com.pluxity.permission.ResourceType
import com.pluxity.permission.dto.ResourceItemResponse
import org.springframework.stereotype.Component

@Component
class FacilityResourceDataProvider(
    private val facilityRepository: FacilityRepository,
) : ResourceDataProvider {
    override val resourceType: ResourceType = ResourceType.FACILITY

    override fun findAllResources(): List<ResourceItemResponse> =
        facilityRepository.findAll().map {
            ResourceItemResponse(
                id = it.id.toString(),
                name = it.name,
            )
        }
}
