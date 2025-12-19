package com.pluxity.user.service.entity

import base.entity.withId
import com.pluxity.user.entity.Role

fun dummyRole(
    id: Long? = 1L,
    name: String = "role",
    description: String? = "description",
) = Role(name, description).withId(id)
