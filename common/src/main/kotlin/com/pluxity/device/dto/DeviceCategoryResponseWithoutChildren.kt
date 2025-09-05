package com.pluxity.device.dto

import com.pluxity.device.entity.DeviceCategory
import com.pluxity.file.dto.FileResponse
import io.swagger.v3.oas.annotations.media.Schema

data class DeviceCategoryResponseWithoutChildren(
    val id: Long?,
    val name: String,
    val parentId: Long?,
    val thumbnailFile: FileResponse,
    @field:Schema(
        description = "depth",
        example = "1",
    )
    val depth: Int,
)

fun DeviceCategory.toDeviceCategoryResponseWithoutChildren(iconFile: FileResponse) =
    DeviceCategoryResponseWithoutChildren(
        id = this.id,
        name = this.name,
        parentId = this.parent?.id,
        thumbnailFile = iconFile,
        depth = this.depth,
    )
