package com.pluxity.patrol.dto

import com.pluxity.patrol.constant.DeviceAction
import com.pluxity.patrol.constant.DeviceType

data class SceneDeviceActionRequest(
    val sceneDeviceActionId: Long? = null,
    val deviceId: String,
    val deviceAction: DeviceAction,
    val deviceType: DeviceType,
    var actionParam: String?,
    var executionOrder: Int?,
)
