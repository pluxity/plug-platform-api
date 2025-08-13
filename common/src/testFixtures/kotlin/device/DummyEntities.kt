package device

import com.pluxity.device.entity.DeviceCategory

fun dummyDeviceCategory(
    name: String? = "category-name",
    iconFileId: Long? = null
) = DeviceCategory(
    name,
    iconFileId
)