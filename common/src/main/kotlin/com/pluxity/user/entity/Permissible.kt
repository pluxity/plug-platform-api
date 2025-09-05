package com.pluxity.user.entity

import com.pluxity.permission.ResourceType

interface Permissible {
    val resourceId: String

    val resourceType: ResourceType
}
