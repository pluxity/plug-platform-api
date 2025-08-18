package com.pluxity.building.entity

import base.entity.withAudit
import com.pluxity.building.Building
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime

fun dummyBuilding(
    name: String = "name",
    code: String = "code",
    description: String = "description",
    drawingFileId: Long? = null,
    thumbnailFileId: Long? = null,
    id: Long? = 1L,
): Building {
    val retBuilding =
        Building(
            name,
            code,
            description,
            drawingFileId,
            thumbnailFileId,
        ).withAudit(LocalDateTime.now())
    ReflectionTestUtils.setField(retBuilding, "id", id)
    return retBuilding
}
