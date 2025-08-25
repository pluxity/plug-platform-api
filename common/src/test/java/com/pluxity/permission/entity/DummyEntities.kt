package com.pluxity.permission.entity

import base.entity.withAudit
import com.pluxity.permission.PermissionGroup

fun dummyPermissionGroup(
    id: Long? = 1L,
    name: String = "name",
    description: String? = "description",
) = PermissionGroup(id, name, description).withAudit()
