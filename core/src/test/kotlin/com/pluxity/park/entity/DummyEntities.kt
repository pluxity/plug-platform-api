package com.pluxity.park.entity

import base.entity.withAudit
import com.pluxity.park.Park
import org.springframework.test.util.ReflectionTestUtils

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
        ).withAudit()
    ReflectionTestUtils.setField(park, "id", id)
    return park
}
