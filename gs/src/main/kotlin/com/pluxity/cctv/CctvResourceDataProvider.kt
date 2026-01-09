package com.pluxity.cctv

import com.pluxity.cctv.repository.CctvRepository
import com.pluxity.permission.ResourceDataProvider
import com.pluxity.permission.ResourceType
import com.pluxity.permission.dto.ResourceItemResponse
import org.springframework.stereotype.Component

@Component
class CctvResourceDataProvider(
    private val cctvRepository: CctvRepository,
) : ResourceDataProvider {
    override val resourceType: ResourceType = ResourceType.CCTV

    override fun findAllResources(): List<ResourceItemResponse> =
        cctvRepository.findAll().map {
            ResourceItemResponse(
                id = it.id,
                name = it.name,
            )
        }
}
