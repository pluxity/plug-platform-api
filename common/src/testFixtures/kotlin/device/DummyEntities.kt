package device

import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType
import com.pluxity.feature.entity.Feature

fun dummyDevice(
    id: String = "deviceId",
    name: String = "deviceName",
    deviceType: DeviceType = DeviceType.TEMP_HUM,
    companyType: DeviceCompanyType = DeviceCompanyType.DAWONDNS,
    feature: Feature? = null,
): Device =
    Device(
        id = id,
        name = name,
        feature = feature,
        deviceType = deviceType,
        companyType = companyType,
    )
