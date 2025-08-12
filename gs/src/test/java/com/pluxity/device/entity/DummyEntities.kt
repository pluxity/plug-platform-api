package com.pluxity.device.entity

import com.pluxity.cctv.entity.Cctv
import com.pluxity.cctv.entity.DeviceCctv
import com.pluxity.device.GsDevice
import com.pluxity.feature.entity.Feature
import com.pluxity.file.constant.FileStatus
import com.pluxity.file.dto.FileResponse
import com.pluxity.global.response.BaseResponse

fun dummyGsDevice(
    id: String = "device-id",
    name: String = "Test Device",
    category: DeviceCategory? = null,
) = GsDevice(
    id,
    category,
    name
)

fun dummyDeviceCategory(
    name: String? = "category-name",
    iconFileId: Long? = null
) = DeviceCategory(
    name,
    iconFileId
)

fun dummyDeviceCctv(
    cctv: Cctv = dummyCctv(),
    device: GsDevice = dummyGsDevice()
) = DeviceCctv(
    device,
    cctv
)

fun dummyCctv(
    id: String = "cctvId",
    name: String = "cctvName",
    url: String = "url",
    category: DeviceCategory? = null,
    feature: Feature? = null
): Cctv = Cctv(id, name, url, category, feature)

fun dummyFileResponse(
    id: Long = 1L,
    url: String = "url",
    originFileName: String = "originFileName",
    contentType: String = "image",
    fileStatus: String = "COMPLETE"
) = FileResponse(
    id, url, originFileName, contentType, fileStatus, dummyBaseResponse()
)

fun dummyBaseResponse() = BaseResponse("", "", "", "")