package cctv

import com.pluxity.cctv.entity.Cctv
import com.pluxity.device.entity.DeviceCategory

fun dummyCctv(
    id: String = "cctvId",
    name: String = "cctvName",
    url: String = "url",
    category: DeviceCategory? = null,
): Cctv = Cctv(id, name, url, category)