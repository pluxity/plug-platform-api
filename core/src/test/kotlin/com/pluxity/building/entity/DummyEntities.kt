package com.pluxity.building.entity

import base.entity.withAudit
import base.entity.withId
import com.pluxity.building.Building

fun dummyBuilding(
    name: String = "name",
    description: String = "description",
    id: Long? = 1L,
): Building {
    val retBuilding =
        Building(
            name,
            description,
        ).withAudit().withId(id)
    return retBuilding
}
