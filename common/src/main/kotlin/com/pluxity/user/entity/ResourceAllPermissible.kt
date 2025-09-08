package com.pluxity.user.entity

import com.pluxity.permission.ResourceType

class ResourceAllPermissible(
    val type: ResourceType,
) : Permissible {
    override val resourceId: String
        get() = "ALL"
    override val resourceType: ResourceType
        get() = type
}
