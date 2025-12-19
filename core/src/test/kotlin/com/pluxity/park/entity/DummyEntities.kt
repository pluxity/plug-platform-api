package com.pluxity.park.entity

import base.entity.withAudit
import base.entity.withId
import com.pluxity.park.Park

fun dummyPark(
    name: String = "name",
    description: String = "description",
    id: Long? = 1L,
    boundary: String? = "boundary",
): Park {
    val park =
        Park(
            name,
            description,
            boundary,
        ).withAudit().withId(id)
    return park
}
