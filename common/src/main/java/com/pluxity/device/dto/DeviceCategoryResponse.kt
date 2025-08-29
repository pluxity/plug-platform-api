package com.pluxity.device.dto

import com.pluxity.device.entity.DeviceCategory
import com.pluxity.file.dto.FileResponse
import io.swagger.v3.oas.annotations.media.Schema

data class DeviceCategoryResponse(
    val id: Long?,
    val name: String,
    val parentId: Long?,
    @field:Schema(
        description = "자식 카테고리 목록",
        example =
            "[{\"id\":2,\"name\":\"서브 카테고리\",\"parentId\":1,\"children\":[],\"thumbnail\":null," +
                "\"assetIds\":[0],\"createdAt\":\"string\",\"updatedAt\":\"string\",\"depth\":2}]",
    )
    val children: List<DeviceCategoryResponse>,
    val thumbnailFile: FileResponse,
    @field:Schema(
        description = "depth",
        example = "1",
    )
    val depth: Int,
)

fun DeviceCategory.toDeviceCategoryResponse(iconFile: FileResponse) =
    DeviceCategoryResponse(
        id = this.id,
        name = this.name,
        parentId = this.parent?.id,
        children = mutableListOf(),
        thumbnailFile = iconFile,
        depth = this.depth,
    )
