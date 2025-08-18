package com.pluxity.park.entity

import base.entity.withAudit
import com.pluxity.park.Park
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime

fun dummyPark(
    name: String = "name",
    description: String = "description",
    id: Long? = 1L,
    boundary: String? = "boundary",
): Park {
    val park =
        Park(
            name,
            "",
            description,
            null,
            null,
            boundary,
        ).withAudit(LocalDateTime.now())
    ReflectionTestUtils.setField(park, "id", id)
    return park
}
