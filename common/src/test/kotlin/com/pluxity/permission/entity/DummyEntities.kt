package com.pluxity.permission.entity

import base.entity.withAudit
import base.entity.withId
import com.pluxity.permission.Permission

fun dummyPermission(
    id: Long = 1L,
    name: String = "name",
    description: String? = "description",
) = Permission(name, description).withAudit().withId(id)
