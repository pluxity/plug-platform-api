package device

import base.entity.withId
import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCategory
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType
import com.pluxity.feature.entity.Feature

fun dummyDeviceCategory(
    id: Long? = null,
    name: String = "category-name",
    iconFileId: Long? = null,
) = DeviceCategory(
    categoryName = name,
    iconFileId = iconFileId,
).withId(id)

fun dummyDevice(
    id: String = "device_id",
    name: String = "device_name",
    feature: Feature? = null,
    category: DeviceCategory? = dummyDeviceCategory(iconFileId = 1L),
    deviceType: DeviceType = DeviceType.TEMP_HUM,
    companyType: DeviceCompanyType = DeviceCompanyType.DAWONDNS,
) = Device(id, name, feature, category, deviceType, companyType)
