package com.pluxity.permission

import com.pluxity.permission.dto.ResourceItemResponse

interface ResourceDataProvider {
    val resourceType: ResourceType

    fun findAllResources(): List<ResourceItemResponse>
}
