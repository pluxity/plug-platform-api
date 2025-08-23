package com.pluxity.building.entity

import base.entity.withAudit
import com.pluxity.building.Building
import org.springframework.test.util.ReflectionTestUtils

fun dummyBuilding(
    name: String = "name",
    description: String = "description",
    id: Long? = 1L,
): Building {
    val retBuilding =
        Building(
            name,
            description,
        ).withAudit()
    ReflectionTestUtils.setField(retBuilding, "id", id)
    return retBuilding
}
